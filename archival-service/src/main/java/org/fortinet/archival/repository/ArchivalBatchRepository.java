package org.fortinet.archival.repository;

import org.fortinet.archival.model.ArchivalBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ArchivalBatchRepository extends JpaRepository<ArchivalBatch, UUID> {
    
    // Find all batches for a specific job
    List<ArchivalBatch> findByJobId(UUID jobId);

    // Find batches by status
    List<ArchivalBatch> findByStatus(ArchivalBatch.BatchStatus status);

    // Find stalled batches (in progress for too long)
    @Query("SELECT b FROM ArchivalBatch b WHERE b.status = 'IN_PROGRESS' " +
           "AND b.updatedAt < :cutoffTime")
    List<ArchivalBatch> findStalledBatches(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find failed batches that can be retried (retry count less than max)
    @Query("SELECT b FROM ArchivalBatch b WHERE b.status = 'FAILED' " +
           "AND b.retryCount < :maxRetries")
    List<ArchivalBatch> findRetryableBatches(@Param("maxRetries") int maxRetries);

    // Count batches by status for a job
    long countByJobIdAndStatus(UUID jobId, ArchivalBatch.BatchStatus status);

    // Find recent failed batches
    List<ArchivalBatch> findByStatusAndCreatedAtAfter(
        ArchivalBatch.BatchStatus status, 
        LocalDateTime after
    );

    // Find incomplete batches (either queued or in progress)
    @Query("SELECT b FROM ArchivalBatch b WHERE b.status IN ('QUEUED', 'IN_PROGRESS')")
    List<ArchivalBatch> findIncompleteBatches();
}