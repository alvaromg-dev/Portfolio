package com.alvaromg.portfolio.infrastructure.in.jsf.model;

public record TelemetryTrackRequest(
    String path,
    String query,
    String source
) {
}
