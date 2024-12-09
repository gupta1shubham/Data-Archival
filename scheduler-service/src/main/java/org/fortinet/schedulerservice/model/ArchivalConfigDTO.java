package org.fortinet.schedulerservice.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArchivalConfigDTO {
    private Long id;
    private String tableName;
    private Integer retentionDays;
    private Integer batchSize;
    private boolean enabled;
    private String cronExpression;
    private LocalDateTime lastRun;
    private Integer deletionDays;
    // Getters and Setters
}