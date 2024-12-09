package org.fortinet.configservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "archival_configs")
public class ArchivalConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "retention_days", nullable = false)
    private Integer retentionDays;

    @Column(name = "deletion_days", nullable = false)
    private Integer deletionDays;

    @Column(name = "batch_size", nullable = false)
    private Integer batchSize;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(name = "last_run")
    private LocalDateTime lastRun;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public Integer getRetentionDays() { return retentionDays; }
    public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }
    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public LocalDateTime getLastRun() { return lastRun; }
    public void setLastRun(LocalDateTime lastRun) { this.lastRun = lastRun; }

    public Integer getDeletionDays() {
        return deletionDays;
    }

    public void setDeletionDays(Integer deletionDays) {
        this.deletionDays = deletionDays;
    }
}