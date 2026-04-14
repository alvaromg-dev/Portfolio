package com.example.sbtemplate.mono.infrastructure.out.jpa.entities.lifecycle;

/**
 * Contract for JPA entity lifecycle hooks.
 * Implementations must provide logic for creation and update events.
 * Note: JPA lifecycle annotations must reside on a class in the inheritance
 * hierarchy (e.g., a @MappedSuperclass). Interfaces cannot carry them.
 */
public interface EntityLifecycle {
    void onCreate();
    void onUpdate();
}
