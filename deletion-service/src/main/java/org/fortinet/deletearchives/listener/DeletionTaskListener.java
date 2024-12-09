package org.fortinet.deletearchives.listener;

import org.fortinet.deletearchives.service.DeletionService;
import org.fortinet.schedulerservice.model.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeletionTaskListener {
    private static final Logger log = LoggerFactory.getLogger(DeletionTaskListener.class);
    
    private final DeletionService deletionService;

    public DeletionTaskListener(DeletionService deletionService) {
        this.deletionService = deletionService;
    }

    @KafkaListener(topics = "${kafka.topics.deletion-tasks}")
    public void handleDeletionTask(TaskMessage task) {
        log.info("Received deletion task for table: {}", task.getTableName());
        deletionService.processDeletionTask(task);
    }
}