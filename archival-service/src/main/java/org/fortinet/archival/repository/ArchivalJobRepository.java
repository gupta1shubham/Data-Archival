package org.fortinet.archival.repository;


import org.fortinet.archival.model.ArchivalJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArchivalJobRepository extends JpaRepository<ArchivalJob, UUID> {
    boolean existsByIdempotencyToken(String idempotencyToken);
}
