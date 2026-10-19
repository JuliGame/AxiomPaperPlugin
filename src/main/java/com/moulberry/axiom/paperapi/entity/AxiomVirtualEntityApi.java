package com.moulberry.axiom.paperapi.entity;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for packet-only entities exposed to Axiom.
 *
 * <p>This class and all types in its signatures intentionally depend only on
 * the JDK. This keeps the API usable by plugins targeting older Java bytecode
 * than the current Axiom server implementation.</p>
 */
public final class AxiomVirtualEntityApi {
    private static final CopyOnWriteArrayList<AxiomVirtualEntityProvider> PROVIDERS =
            new CopyOnWriteArrayList<>();

    private AxiomVirtualEntityApi() {
    }

    public static AxiomVirtualEntityRegistration register(AxiomVirtualEntityProvider provider) {
        Objects.requireNonNull(provider, "provider");
        if (!PROVIDERS.addIfAbsent(provider)) {
            throw new IllegalArgumentException("Virtual entity provider is already registered");
        }
        return new Registration(provider);
    }

    public static Resolved find(UUID playerUuid, UUID entityUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(entityUuid, "entityUuid");
        for (AxiomVirtualEntityProvider provider : PROVIDERS) {
            AxiomVirtualEntitySnapshot snapshot = provider.find(playerUuid, entityUuid);
            if (snapshot != null) {
                return new Resolved(provider, snapshot);
            }
        }
        return null;
    }

    public record Resolved(
            AxiomVirtualEntityProvider provider,
            AxiomVirtualEntitySnapshot snapshot
    ) {
        public Resolved {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(snapshot, "snapshot");
        }
    }

    private static final class Registration implements AxiomVirtualEntityRegistration {
        private AxiomVirtualEntityProvider provider;

        private Registration(AxiomVirtualEntityProvider provider) {
            this.provider = provider;
        }

        @Override
        public synchronized void close() {
            if (provider != null) {
                PROVIDERS.remove(provider);
                provider = null;
            }
        }
    }
}
