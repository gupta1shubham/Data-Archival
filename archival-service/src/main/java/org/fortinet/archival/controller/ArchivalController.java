package org.fortinet.archival.controller;

import org.fortinet.archival.service.ArchivalService;
import org.fortinet.schedulerservice.model.TaskMessage;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/archive")
public class ArchivalController {
    private final ArchivalService archivalService;

    public ArchivalController(ArchivalService archivalService) {
        this.archivalService = archivalService;
    }

    @PostMapping("/{tableName}")
    public UUID triggerArchival(@RequestBody TaskMessage taskMessage) {
        return archivalService.processArchivalTask(taskMessage);
    }
}
