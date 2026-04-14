package com.example.sbtemplate.mono.infrastructure.in.jsf.model;

public record TelemetryTrackRequest(
    String path,
    String query,
    String source
) {
}
