package com.moulberry.axiom.paperapi.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable state exposed to Axiom for a virtual entity.
 *
 * @param entityTypeKey namespaced vanilla entity type represented by the packet entity
 * @param worldUuid UUID of the world containing the entity
 * @param entityData entity-specific NBT; identity, position and rotation are
 *                   supplied by Axiom and must not be included
 */
public record AxiomVirtualEntitySnapshot(
        String entityTypeKey,
        UUID worldUuid,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        AxiomNbtCompound entityData
) {
    public AxiomVirtualEntitySnapshot {
        Objects.requireNonNull(entityTypeKey, "entityTypeKey");
        Objects.requireNonNull(worldUuid, "worldUuid");
        Objects.requireNonNull(entityData, "entityData");
        if (entityTypeKey.isBlank()) {
            throw new IllegalArgumentException("entityTypeKey must not be blank");
        }
    }
}
