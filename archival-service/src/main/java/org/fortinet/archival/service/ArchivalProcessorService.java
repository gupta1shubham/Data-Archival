package org.fortinet.archival.service;

import org.fortinet.archival.model.ArchivalBatch;
import org.fortinet.archival.model.ArchivalJob;
import org.fortinet.archival.repository.ArchivalBatchRepository;
import org.fortinet.archival.repository.ArchivalJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArchivalProcessorService {
    private static final Logger log = LoggerFactory.getLogger(ArchivalProcessorService.class);


    private static final String SELECT_QUEUED_RECORDS =
            "SELECT r.* FROM %s_archive_queue q " +
            "JOIN %s r ON q.id = r.id " +
            "WHERE q.batch_id = ? AND q.status = 'PENDING'";

    private static final String UPDATE_QUEUE_STATUS =
            "UPDATE %s_archive_queue SET status = ?, last_retry = ?, error_message = ? " +
            "WHERE id = ? AND batch_id = ?";

    private final JdbcTemplate sourceJdbcTemplate;
    private final JdbcTemplate archivalJdbcTemplate;
    private final ArchivalBatchRepository batchRepository;
    private final ArchivalJobRepository archivalJobRepository;
    private final EncryptionService encryptionService;

    public ArchivalProcessorService(
            @Qualifier("primaryJdbcTemplate") JdbcTemplate sourceJdbcTemplate,
            @Qualifier("archivalJdbcTemplate") JdbcTemplate archivalJdbcTemplate,
            ArchivalBatchRepository batchRepository, ArchivalJobRepository archivalJobRepository, EncryptionService encryptionService) {
        this.sourceJdbcTemplate = sourceJdbcTemplate;
        this.archivalJdbcTemplate = archivalJdbcTemplate;
        this.batchRepository = batchRepository;
        this.archivalJobRepository = archivalJobRepository;
        this.encryptionService = encryptionService;
    }

    @Scheduled(fixedDelay = 60000)
    public void testScheduledTask() {
        System.out.println("Scheduled task running at " + LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 60000) // Runs every minute
    public void processQueuedBatches() {
        List<ArchivalBatch> queuedBatches = batchRepository.findByStatus(ArchivalBatch.BatchStatus.QUEUED);
        
        for (ArchivalBatch batch : queuedBatches) {
            try {
                processBatch(batch);
            } catch (Exception e) {
                log.error("Error processing batch {}", batch.getBatchId(), e);
                markBatchAsFailed(batch, e.getMessage());
            }
        }
    }

    @Transactional
    void processBatch(ArchivalBatch batch) {
        log.info("Processing batch {} for job {}", batch.getBatchId(), batch.getJobId());
        
        // Update batch status to IN_PROGRESS
        batch.setStatus(ArchivalBatch.BatchStatus.IN_PROGRESS);
        batchRepository.save(batch);

        // Get the table name from the job
        String tableName = getTableNameFromJob(batch.getJobId());
        
        // Fetch records for this batch
        List<Map<String, Object>> records = getQueuedRecords(tableName, batch.getBatchId());
        
        int successCount = 0;
        int failureCount = 0;

        for (Map<String, Object> record : records) {
            try {
                // Archive individual record
                archiveRecord(tableName, record, batch.getBatchId());
                updateQueueStatus(tableName, record, "COMPLETED", null);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to archive record {} in batch {}", record.get("id"), batch.getBatchId(), e);
                updateQueueStatus(tableName, record, "FAILED", e.getMessage());
                failureCount++;
            }
        }

        // Update batch status
        batch.setSuccessfulRecords(successCount);
        batch.setFailedRecords(failureCount);
        
        if (failureCount == 0) {
            batch.setStatus(ArchivalBatch.BatchStatus.COMPLETED);
        } else if (successCount == 0) {
            batch.setStatus(ArchivalBatch.BatchStatus.FAILED);
        } else {
            batch.setStatus(ArchivalBatch.BatchStatus.PARTIALLY_COMPLETED);
        }
        
        batch.setCompletedAt(LocalDateTime.now());
        batchRepository.save(batch);

        // If all records were successful, clean up source records
        if (failureCount == 0) {
            deleteSourceRecords(tableName, records);
        }
    }

    private List<Map<String, Object>> getQueuedRecords(String tableName, UUID batchId) {
        String sql = String.format(SELECT_QUEUED_RECORDS, tableName, tableName);
        return sourceJdbcTemplate.queryForList(sql, batchId);
    }

    private void updateQueueStatus(String tableName, Map<String, Object> record, String status, String errorMessage) {
        String sql = String.format(UPDATE_QUEUE_STATUS, tableName);
        sourceJdbcTemplate.update(sql, status, LocalDateTime.now(), errorMessage, record.get("id"), record.get("batch_id"));
    }

    private void archiveRecord(String tableName, Map<String, Object> record, UUID batchId) {
        String insertSql = String.format(
            "INSERT INTO %s (id, data, created_date, last_updated_date, is_active, archived_date, batch_id, archival_status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            tableName);
        String encryptedData = encryptionService.encrypt((String) record.get("data"));

        archivalJdbcTemplate.update(insertSql,
            record.get("id"),
            encryptedData,
            record.get("created_date"),
            record.get("last_updated_date"),
            false,
            LocalDateTime.now(),
            batchId,
            "ARCHIVED");
    }

    private void deleteSourceRecords(String tableName, List<Map<String, Object>> records) {
        String ids = records.stream()
                .map(r -> r.get("id").toString())
                .collect(Collectors.joining(","));

        String deleteSql = String.format("DELETE FROM %s WHERE id IN (%s)", tableName, ids);
        sourceJdbcTemplate.update(deleteSql);
    }

    private void markBatchAsFailed(ArchivalBatch batch, String errorMessage) {
        batch.setStatus(ArchivalBatch.BatchStatus.FAILED);
        batch.setErrorMessage(errorMessage);
        batch.setCompletedAt(LocalDateTime.now());
        batchRepository.save(batch);
    }

    private String getTableNameFromJob(UUID jobId) {
        // Implementation to get table name from job repository
        return archivalJobRepository.findById(jobId)
            .map(ArchivalJob::getTableName)
            .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
    }
}