package com.moulberry.axiom.paperapi.entity;

import java.util.UUID;

/**
 * Supplies packet-only entities to Axiom's entity inspector and editor.
 *
 * <p>Providers are queried only when no real server entity exists for the
 * requested UUID. Implementations must enforce their own edit permissions in
 * {@link #applyMutation(UUID, UUID, AxiomVirtualEntityMutation)}.</p>
 */
public interface AxiomVirtualEntityProvider {

    AxiomVirtualEntitySnapshot find(UUID playerUuid, UUID entityUuid);

    void applyMutation(
            UUID playerUuid,
            UUID entityUuid,
            AxiomVirtualEntityMutation mutation
    );
}
