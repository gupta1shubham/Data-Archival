package org.fortinet.deletearchives.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


import lombok.Data;

@Entity
@Table(name = "deletion_jobs")
@Data
public class DeletionJob {
    @Id
    private UUID jobId;

    @Column(nullable = false)
    private String tableName;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private String idempotencyToken;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errorMessage;

    private Integer recordsDeleted;

    public enum JobStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}