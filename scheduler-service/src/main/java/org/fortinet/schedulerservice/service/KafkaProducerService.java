package org.fortinet.schedulerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.fortinet.schedulerservice.model.JobType;
import org.fortinet.schedulerservice.model.TaskMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, TaskMessage> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, TaskMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendArchivalTask(TaskMessage task, JobType jobType) {
        try {
            CompletableFuture<SendResult<String, TaskMessage>> future =
                    kafkaTemplate.send(jobType.getType(), task);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent archival task for table: {} with offset: {}",
                            task.getTableName(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send archival task for table: {}",
                            task.getTableName(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending archival task", e);
            throw e;
        }
    }


}