package com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.telemetry;

import com.alvaromg.portfolio.infrastructure.out.jpa.entities.telemetry.TelemetryLoginEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryLoginJpaRepository extends JpaRepository<TelemetryLoginEntity, UUID> {

    List<TelemetryLoginEntity> findTop500ByOrderByLoggedAtDesc();
}

