package org.fortinet.archival.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArchivalQueueService {
    private static final Logger log = LoggerFactory.getLogger(ArchivalQueueService.class);

    private static final String INSERT_QUEUE_TEMPLATE =
            "INSERT INTO %s_archive_queue (id, batch_id, status, retry_count, last_retry) " +
                    "VALUES (?, ?, 'PENDING', 0, ?)";

    private final JdbcTemplate sourceJdbcTemplate;

    public ArchivalQueueService(@Qualifier("primaryJdbcTemplate") JdbcTemplate sourceJdbcTemplate) {
        this.sourceJdbcTemplate = sourceJdbcTemplate;
    }

    @Transactional
    public void queueRecordsForArchival(String tableName, List<Map<String, Object>> records, UUID batchId) {
        String insertSql = String.format(INSERT_QUEUE_TEMPLATE, tableName);
        LocalDateTime now = LocalDateTime.now();

        sourceJdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> record = records.get(i);
                ps.setLong(1, ((Number) record.get("id")).longValue());
                ps.setObject(2, batchId);
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }
}