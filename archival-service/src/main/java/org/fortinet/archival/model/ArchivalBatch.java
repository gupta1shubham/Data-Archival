package org.fortinet.archival.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "archival_batches")
public class ArchivalBatch {
    
    public enum BatchStatus {
        QUEUED,        // Initial state when batch is created
        IN_PROGRESS,   // Being processed by worker
        COMPLETED,     // All records successfully archived
        FAILED,        // Batch failed, needs investigation
        PARTIALLY_COMPLETED  // Some records succeeded, some failed
    }

    @Id
    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "record_count", nullable = false)
    private int recordCount;

    @Column(name = "successful_records")
    private int successfulRecords = 0;

    @Column(name = "failed_records")
    private int failedRecords = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
}