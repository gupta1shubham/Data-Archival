package org.fortinet.viewarchival.controller;


import org.fortinet.viewarchival.service.ArchiveService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestController
@RequestMapping("/api/view")
public class ArchiveController {
    private final ArchiveService archiveService;

    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GetMapping("/{tableName}")
    public ResponseEntity<?> viewArchivedData(
            @PathVariable String tableName,
            @RequestParam(required = false) Map<String, String> filters,
            Pageable pageable) throws AccessDeniedException {
        
        Page<?> data = archiveService.getArchivedData(tableName, filters, pageable);
        return ResponseEntity.ok(data);
    }
}
