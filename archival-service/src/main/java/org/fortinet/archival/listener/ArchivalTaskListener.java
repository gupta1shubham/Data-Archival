package org.fortinet.archival.listener;

import lombok.extern.slf4j.Slf4j;
import org.fortinet.archival.service.ArchivalService;
import org.fortinet.schedulerservice.model.TaskMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ArchivalTaskListener {

    private final ArchivalService archivalService;

    public ArchivalTaskListener(ArchivalService archivalService) {
        this.archivalService = archivalService;
    }

    @KafkaListener(topics = "${kafka.topics.archival-tasks}")
    public void handleArchivalTask(@Payload TaskMessage task) {
        log.info("Received archival task for table: {}", task.getTableName());
        try {
            archivalService.processArchivalTask(task);
        } catch (Exception e) {
            log.error("Error processing archival task for table: {}", task.getTableName(), e);
            throw e;
        }
    }
}