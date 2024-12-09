package org.fortinet.configservice.controller;

import org.fortinet.configservice.model.ArchivalConfig;
import org.fortinet.configservice.service.ConfigurationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
public class ConfigurationController {
    private final ConfigurationService configService;

    public ConfigurationController(ConfigurationService configService) {
        this.configService = configService;
    }

    @GetMapping("/enabled")
    public List<ArchivalConfig> getEnabledConfigs() {
        return configService.getEnabledConfigurations();
    }

    @PutMapping("/{id}/last-run")
    public void updateLastRun(@PathVariable Long id) {
        configService.updateLastRun(id);
    }

    @PostMapping
    public ResponseEntity<ArchivalConfig> createArchivalConfig(@RequestBody ArchivalConfig archivalConfig) {
        ArchivalConfig savedConfig = configService.saveArchivalConfig(archivalConfig);
        return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
    }
}

