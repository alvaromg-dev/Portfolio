package com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.telemetry;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.telemetry.TelemetryVisitEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryVisitJpaRepository extends JpaRepository<TelemetryVisitEntity, UUID> {

    List<TelemetryVisitEntity> findTop500ByOrderByVisitedAtDesc();
}
