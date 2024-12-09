package org.fortinet.configservice.service;


import org.fortinet.configservice.model.ArchivalConfig;
import org.fortinet.configservice.repository.ArchivalConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConfigurationService {
    private final ArchivalConfigRepository configRepository;

    public ConfigurationService(ArchivalConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public List<ArchivalConfig> getEnabledConfigurations() {
        return configRepository.findByEnabledTrue();
    }

    @Transactional
    public void updateLastRun(Long id) {
        ArchivalConfig archivalConfig = configRepository.findById(id).orElse(null);
        archivalConfig.setLastRun(LocalDateTime.now());
        configRepository.save(archivalConfig);
    }

    public ArchivalConfig saveArchivalConfig(ArchivalConfig archivalConfig) {
        return configRepository.save(archivalConfig);
    }
}