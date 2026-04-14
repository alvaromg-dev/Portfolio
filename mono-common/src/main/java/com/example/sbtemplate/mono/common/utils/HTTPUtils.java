/*
package com.example.sptemplate.common.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import lombok.Data;

public class HTTPUtils {

    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

    private static <T> HttpResponse sendRequest(String meºthod, String url, T body, String bearerToken) {

        // Initialize the request builder
        HttpRequest.Builder builder = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10));

        // Body publisher: serialize object to JSON if provided
        if (body != null) {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body))
                   .header("Content-Type", "application/json");
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        // Token
        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }

        // Send the request
        try {
            return CLIENT.send(builder.build(), BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw e;
        }
    }
}
*/