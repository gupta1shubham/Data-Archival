package org.fortinet.configservice.repository;

import org.fortinet.configservice.model.ArchivalConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchivalConfigRepository extends JpaRepository<ArchivalConfig, Long> {
    List<ArchivalConfig> findByEnabledTrue();
}
