package com.example.sbtemplate.mono.infrastructure.out.jpa.entities.lifecycle;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Base class that wires JPA lifecycle callbacks and delegates to
 * the concrete entity via {@link EntityLifecycle} methods.
 */
@MappedSuperclass
public abstract class AuditableEntity implements EntityLifecycle {

    @PrePersist
    private void prePersistCallback() {
        onCreate();
    }

    @PreUpdate
    private void preUpdateCallback() {
        onUpdate();
    }
}
