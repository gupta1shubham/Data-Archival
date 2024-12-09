package org.fortinet.schedulerservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskMessage implements Serializable {
    private UUID taskId;
    private String tableName;
    private Integer batchSize;
    private Integer retentionDays;
    private String idempotencyToken;
}
