package org.fortinet.schedulerservice.client;

import org.fortinet.schedulerservice.model.ArchivalConfigDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(name = "config-service", url = "${config.service.url}")
public interface ConfigurationServiceClient {
    @GetMapping("/api/configs/enabled")
    List<ArchivalConfigDTO> getEnabledConfigs();

    @PutMapping("/api/configs/{id}/last-run")
    void updateLastRun(@PathVariable("id") Long id);
}