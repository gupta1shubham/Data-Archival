package org.fortinet.schedulerservice.service;

import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.fortinet.schedulerservice.client.ConfigurationServiceClient;
import org.fortinet.schedulerservice.model.ArchivalConfigDTO;
import org.fortinet.schedulerservice.model.TaskMessage;
import org.fortinet.schedulerservice.model.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SchedulerService {
    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    private final ConfigurationServiceClient configClient;
    private final KafkaProducerService kafkaProducerService;

    public SchedulerService(
            ConfigurationServiceClient configClient,
            KafkaProducerService kafkaProducerService) {
        this.configClient = configClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Scheduled(fixedDelayString = "${scheduler.check-interval:60000}")
    public void scheduleJobs() {
        log.info("Starting job scheduling check");
        try {
            List<ArchivalConfigDTO> configs = configClient.getEnabledConfigs();
            processConfigs(configs);
        } catch (Exception e) {
            log.error("Error during job scheduling", e);
        }
    }

    private void processConfigs(List<ArchivalConfigDTO> configs) {
        for (ArchivalConfigDTO config : configs) {
            try {
                if (shouldRunJob(config)) {
                    scheduleArchivalTask(config);
                    scheduleDeletionTask(config);
                    configClient.updateLastRun(config.getId());
                    log.debug("Successfully scheduled tasks for table: {}", config.getTableName());
                }
            } catch (Exception e) {
                log.error("Error processing config for table: {}", config.getTableName(), e);
            }
        }
    }

    private boolean shouldRunJob(ArchivalConfigDTO config) {
        if (config.getLastRun() == null) {
            return true;
        }

        try {
            CronExpression cronExpression = CronExpression.parse(config.getCronExpression());
            LocalDateTime nextRun = cronExpression.next(config.getLastRun());
            return LocalDateTime.now().isAfter(nextRun);
        } catch (Exception e) {
            log.error("Error parsing cron expression for table: {}", config.getTableName(), e);
            return false;
        }
    }

    private void scheduleArchivalTask(ArchivalConfigDTO config) {
        try {
            validateArchivalConfig(config);

            TaskMessage task = new TaskMessage();
            task.setTaskId(UUID.randomUUID());
            task.setTableName(config.getTableName());
            task.setBatchSize(config.getBatchSize());
            task.setRetentionDays(config.getRetentionDays());
            task.setIdempotencyToken(generateIdempotencyToken(config.getTableName(), "ARCHIVAL"));

            log.info("Scheduling archival task for table: {} with retention days: {}",
                    config.getTableName(), config.getRetentionDays());
            kafkaProducerService.sendArchivalTask(task, JobType.ARCHIVAL);
        } catch (Exception e) {
            log.error("Error scheduling archival task for table: {}", config.getTableName(), e);
            throw new SchedulingException("Failed to schedule archival task", e);
        }
    }

    private void scheduleDeletionTask(ArchivalConfigDTO config) {
        try {
            validateDeletionConfig(config);

            TaskMessage task = new TaskMessage();
            task.setTaskId(UUID.randomUUID());
            task.setTableName(config.getTableName());
            task.setBatchSize(config.getBatchSize());
            task.setRetentionDays(config.getDeletionDays());
            task.setIdempotencyToken(generateIdempotencyToken(config.getTableName(), "DELETION"));

            log.info("Scheduling deletion task for table: {} with delete interval: {}",
                    config.getTableName(), config.getDeletionDays());
            kafkaProducerService.sendArchivalTask(task, JobType.DELETION);
        } catch (Exception e) {
            log.error("Error scheduling deletion task for table: {}", config.getTableName(), e);
            throw new SchedulingException("Failed to schedule deletion task", e);
        }
    }

    private void validateArchivalConfig(ArchivalConfigDTO config) {
        if (config.getRetentionDays() == null || config.getRetentionDays() <= 0) {
            throw new InvalidConfigurationException("Invalid retention days for table: " + config.getTableName());
        }
        if (config.getBatchSize() == null || config.getBatchSize() <= 0) {
            throw new InvalidConfigurationException("Invalid batch size for table: " + config.getTableName());
        }
    }

    private void validateDeletionConfig(ArchivalConfigDTO config) {
        if (config.getDeletionDays() == null || config.getDeletionDays() <= 0) {
            throw new InvalidConfigurationException("Invalid delete interval for table: " + config.getTableName());
        }
        if (config.getBatchSize() == null || config.getBatchSize() <= 0) {
            throw new InvalidConfigurationException("Invalid batch size for table: " + config.getTableName());
        }

    }

    private String generateIdempotencyToken(String tableName, String jobType) {
        return String.format("%s-%s-%s", jobType, tableName, UUID.randomUUID());
    }
}