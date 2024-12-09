package org.fortinet.archival.service;

import org.fortinet.archival.exception.ArchivalProcessingException;
import org.fortinet.archival.model.ArchivalBatch;
import org.fortinet.archival.model.ArchivalJob;
import org.fortinet.archival.model.JobStatus;
import org.fortinet.archival.repository.ArchivalBatchRepository;
import org.fortinet.archival.repository.ArchivalJobRepository;
import org.fortinet.schedulerservice.model.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Service
public class ArchivalService {
    private static final Logger log = LoggerFactory.getLogger(ArchivalService.class);

    private static final String TABLE_CREATE_TEMPLATE =
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "    id BIGINT NOT NULL PRIMARY KEY," +
                    "    data VARCHAR(255)," +
                    "    created_date TIMESTAMP," +
                    "    last_updated_date TIMESTAMP," +
                    "    is_active BOOLEAN DEFAULT TRUE," +
                    "    archived_date TIMESTAMP," +
                    "    batch_id UUID," +
                    "    archival_status VARCHAR(20)" +
                    ")";

    private static final String SELECT_RECORDS_TEMPLATE =
            "SELECT id, data, created_date, last_updated_date, is_active " +
                    "FROM %s " +
                    "WHERE is_active = true " +
                    "AND last_updated_date <= ? " +
                    "AND id NOT IN (SELECT id FROM %s_archive_queue WHERE status != 'FAILED') " +
                    "LIMIT ?";

    private static final String INSERT_ARCHIVE_TEMPLATE =
            "INSERT INTO %s (id, data, created_date, last_updated_date, is_active, archived_date, batch_id, archival_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String CREATE_QUEUE_TABLE_TEMPLATE =
            "CREATE TABLE IF NOT EXISTS %s_archive_queue (" +
                    "    id BIGINT NOT NULL," +
                    "    batch_id UUID NOT NULL," +
                    "    status VARCHAR(20)," +
                    "    retry_count INT DEFAULT 0," +
                    "    last_retry TIMESTAMP," +
                    "    error_message TEXT," +
                    "    PRIMARY KEY (id, batch_id)" +
                    ")";

    private final JdbcTemplate sourceJdbcTemplate;
    private final JdbcTemplate archivalJdbcTemplate;
    private final ArchivalJobRepository jobRepository;
    private final ArchivalBatchRepository batchRepository;
    private final ArchivalQueueService queueService;

    public ArchivalService(
            @Qualifier("primaryJdbcTemplate") JdbcTemplate sourceJdbcTemplate,
            @Qualifier("archivalJdbcTemplate") JdbcTemplate archivalJdbcTemplate,
            ArchivalJobRepository jobRepository,
            ArchivalBatchRepository batchRepository,
            ArchivalQueueService queueService) {
        this.sourceJdbcTemplate = sourceJdbcTemplate;
        this.archivalJdbcTemplate = archivalJdbcTemplate;
        this.jobRepository = jobRepository;
        this.batchRepository = batchRepository;
        this.queueService = queueService;
    }

    @Transactional
    public UUID processArchivalTask(TaskMessage task) {
        if (jobRepository.existsByIdempotencyToken(task.getIdempotencyToken())) {
            log.info("Task already processed: {}", task.getTaskId());
            return task.getTaskId();
        }

        ArchivalJob job = createJob(task);
        try {
            ensureArchivalInfrastructure(task.getTableName());

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(task.getRetentionDays());
            List<Map<String, Object>> records = queryRecordsToArchive(task.getTableName(),
                    cutoffDate,
                    task.getBatchSize());

            if (!records.isEmpty()) {
                ArchivalBatch batch = createArchivalBatch(job, records.size());
                queueRecordsForArchival(task.getTableName(), records, batch);

                completeJob(job, records.size());
                log.info("Queued {} records for archival from table {}", records.size(), task.getTableName());
            } else {
                completeJob(job, 0);
                log.info("No records to archive in table {}", task.getTableName());
            }
            return job.getJobId();
        } catch (Exception e) {
            log.error("Error processing archival task for table: " + task.getTableName(), e);
            failJob(job, e.getMessage());
            throw new ArchivalProcessingException("Failed to process archival task", e);
        }
    }

    private void ensureArchivalInfrastructure(String tableName) {
        // Create archive table in remote DB
        archivalJdbcTemplate.execute(String.format(TABLE_CREATE_TEMPLATE, tableName));

        // Create local queue table
        sourceJdbcTemplate.execute(String.format(CREATE_QUEUE_TABLE_TEMPLATE, tableName));
    }

    @Retryable(
            value = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void queueRecordsForArchival(String tableName, List<Map<String, Object>> records, ArchivalBatch batch) {
        queueService.queueRecordsForArchival(tableName, records, batch.getBatchId());
    }

    private ArchivalBatch createArchivalBatch(ArchivalJob job, int recordCount) {
        ArchivalBatch batch = new ArchivalBatch();
        batch.setBatchId(UUID.randomUUID());
        batch.setJobId(job.getJobId());
        batch.setRecordCount(recordCount);
        batch.setStatus(ArchivalBatch.BatchStatus.QUEUED);
        batch.setCreatedAt(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    // Rest of the helper methods remain similar but adapted for batch processing
    private List<Map<String, Object>> queryRecordsToArchive(String tableName,
                                                            LocalDateTime cutoffDate,
                                                            int batchSize) {
        String query = String.format(SELECT_RECORDS_TEMPLATE, tableName, tableName);
        return sourceJdbcTemplate.queryForList(query, cutoffDate, batchSize);
    }

    private ArchivalJob createJob(TaskMessage task) {
        ArchivalJob job = new ArchivalJob();
        job.setJobId(task.getTaskId());
        job.setTableName(task.getTableName());
        job.setStatus(ArchivalJob.JobStatus.IN_PROGRESS);
        job.setIdempotencyToken(task.getIdempotencyToken());
        job.setStartTime(LocalDateTime.now());
        return jobRepository.save(job);
    }

    private void completeJob(ArchivalJob job, int recordCount) {
        job.setStatus(ArchivalJob.JobStatus.QUEUED);
        job.setEndTime(LocalDateTime.now());
        job.setRecordsProcessed(recordCount);
        jobRepository.save(job);
    }

    private void failJob(ArchivalJob job, String errorMessage) {
        job.setStatus(ArchivalJob.JobStatus.FAILED);
        job.setEndTime(LocalDateTime.now());
        job.setErrorMessage(errorMessage);
        jobRepository.save(job);
    }
}