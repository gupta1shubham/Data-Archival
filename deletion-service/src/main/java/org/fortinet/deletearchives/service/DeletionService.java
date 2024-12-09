package org.fortinet.deletearchives.service;


import org.fortinet.deletearchives.exception.DeletionProcessingException;
import org.fortinet.deletearchives.model.DeletionJob;
import org.fortinet.deletearchives.repository.DeletionJobRepository;
import org.fortinet.schedulerservice.model.TaskMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DeletionService {
    private static final Logger log = LoggerFactory.getLogger(DeletionService.class);

    private static final String DELETE_ARCHIVAL_TEMPLATE =
            "WITH cte AS (" +
                    "    SELECT ctid FROM %s WHERE last_updated_date <= ? LIMIT ?" +
                    ") " +
                    "DELETE FROM %s WHERE ctid IN (SELECT ctid FROM cte)";

    private final JdbcTemplate archivalJdbcTemplate;
    private final DeletionJobRepository jobRepository;

    public DeletionService(
            JdbcTemplate archivalJdbcTemplate,
            DeletionJobRepository jobRepository) {
        this.archivalJdbcTemplate = archivalJdbcTemplate;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public UUID processDeletionTask(TaskMessage task) {
        if (jobRepository.existsByIdempotencyToken(task.getIdempotencyToken())) {
            log.info("Task already processed: {}", task.getTaskId());
            return task.getTaskId();
        }

        DeletionJob job = createJob(task);
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(task.getRetentionDays());

            // Delete from archival database
            int deletedCount = deleteFromArchival(task.getTableName(),
                    cutoffDate,
                    task.getBatchSize());

            completeJob(job, deletedCount);
            log.info("Deleted {} records from archival database for table {}",
                    deletedCount, task.getTableName());

            return job.getJobId();
        } catch (Exception e) {
            log.error("Error processing deletion task for table: " + task.getTableName(), e);
            failJob(job, e.getMessage());
            throw new DeletionProcessingException("Failed to process deletion task", e);
        }
    }

    private int deleteFromArchival(String tableName, LocalDateTime cutoffDate, int batchSize) {
        // Format the DELETE SQL to replace %s with the table name
        String deleteSql = String.format(DELETE_ARCHIVAL_TEMPLATE, tableName, tableName);
        // Execute the delete query
        return archivalJdbcTemplate.update(deleteSql, cutoffDate, batchSize);
    }

    private DeletionJob createJob(TaskMessage task) {
        DeletionJob job = new DeletionJob();
        job.setJobId(task.getTaskId());
        job.setTableName(task.getTableName());
        job.setStatus(DeletionJob.JobStatus.IN_PROGRESS);
        job.setIdempotencyToken(task.getIdempotencyToken());
        job.setStartTime(LocalDateTime.now());
        return jobRepository.save(job);
    }

    private void completeJob(DeletionJob job, int deletedCount) {
        job.setStatus(DeletionJob.JobStatus.COMPLETED);
        job.setEndTime(LocalDateTime.now());
        job.setRecordsDeleted(deletedCount);
        jobRepository.save(job);
    }

    private void failJob(DeletionJob job, String errorMessage) {
        job.setStatus(DeletionJob.JobStatus.FAILED);
        job.setEndTime(LocalDateTime.now());
        job.setErrorMessage(errorMessage);
        jobRepository.save(job);
    }
}