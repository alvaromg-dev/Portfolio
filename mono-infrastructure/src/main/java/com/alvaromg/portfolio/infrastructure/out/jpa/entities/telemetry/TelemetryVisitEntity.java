package com.alvaromg.portfolio.infrastructure.out.jpa.entities.telemetry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "telemetry_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryVisitEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "visitor_id", nullable = false, length = 64)
    private String visitorId;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "country", length = 64)
    private String country;

    @Column(name = "region", length = 128)
    private String region;

    @Column(name = "city", length = 128)
    private String city;

    @Column(name = "browser", length = 64)
    private String browser;

    @Column(name = "device_type", length = 32)
    private String deviceType;

    @Column(name = "operating_system", length = 64)
    private String operatingSystem;

    @Column(name = "user_agent", length = 2000)
    private String userAgent;

    @Column(name = "source", length = 512)
    private String source;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "query", length = 1000)
    private String query;

    @Column(name = "accept_language", length = 255)
    private String acceptLanguage;

    @Column(name = "visited_at", nullable = false, length = 40)
    private String visitedAt;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (visitedAt == null || visitedAt.isBlank()) {
            visitedAt = Instant.now().toString();
        }
    }
}
