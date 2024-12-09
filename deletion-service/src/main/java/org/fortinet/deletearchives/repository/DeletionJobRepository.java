package org.fortinet.deletearchives.repository;

import org.fortinet.deletearchives.model.DeletionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletionJobRepository extends JpaRepository<DeletionJob, Long> {

    boolean existsByIdempotencyToken(String idempotencyToken);
}
