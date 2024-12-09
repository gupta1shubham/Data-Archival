package org.fortinet.viewarchival.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArchiveService {
    private final JdbcTemplate jdbcTemplate;
    private final EncryptionService encryptionService;

    public ArchiveService(JdbcTemplate jdbcTemplate, EncryptionService encryptionService) {
        this.jdbcTemplate = jdbcTemplate;
        this.encryptionService = encryptionService;
    }

    public Page<?> getArchivedData(String tableName, Map<String, String> filters, Pageable pageable) throws AccessDeniedException {
        // Check if user has access to the table
        if (!hasTableAccess(tableName)) {
            throw new AccessDeniedException("You don't have permission to access this table");
        }

        // Build the SQL query with pagination and filtering
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT * FROM ").append(tableName);

        // Add filters if present
        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(buildWhereClause(filters, params));
        }

        // Add pagination
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        // Execute query
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        // Decrypt the data field for each record
        List<Map<String, Object>> decryptedData = data.stream()
                .map(record -> {
                    Map<String, Object> decryptedRecord = new HashMap<>(record);
                    if (record.get("data") != null) {
                        String encryptedData = (String) record.get("data");
                        try {
                            String decryptedValue = encryptionService.decrypt(encryptedData);
                            decryptedRecord.put("data", decryptedValue);
                        } catch (Exception e) {
                            log.error("Error decrypting data for record {}: {}", record.get("id"), e.getMessage());
                            decryptedRecord.put("data", "**DECRYPTION_ERROR**");
                        }
                    }
                    return decryptedRecord;
                })
                .collect(Collectors.toList());

        // Get total count for pagination
        String countSql = "SELECT COUNT(*) FROM " + tableName;
        if (filters != null && !filters.isEmpty()) {
            countSql += " WHERE " + buildWhereClause(filters, new ArrayList<>());
        }
        long total = jdbcTemplate.queryForObject(countSql, Long.class);

        return new PageImpl<>(decryptedData, pageable, total);
    }

    private boolean hasTableAccess(String tableName) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if user has ROLE_ADMIN or role matching table name
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> 
                    authority.getAuthority().equals("ROLE_ADMIN") ||
                    authority.getAuthority().equals("ROLE_" + tableName.toUpperCase())
                );
    }

    private String buildWhereClause(Map<String, String> filters, List<Object> params) {
        return filters.entrySet().stream()
                .map(entry -> {
                    params.add(entry.getValue());
                    return entry.getKey() + " = ?";
                })
                .collect(Collectors.joining(" AND "));
    }
}