package com.moulberry.axiom.paperapi.entity;

import java.util.Objects;
import java.util.Set;

public record AxiomVirtualEntityMutation(
        Position position,
        AxiomNbtCompound mergeData
) {
    public AxiomVirtualEntityMutation {
        Objects.requireNonNull(mergeData, "mergeData");
    }

    public enum Relative {
        X,
        Y,
        Z,
        YAW,
        PITCH
    }

    public record Position(
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            Set<Relative> relative
    ) {
        public Position {
            Objects.requireNonNull(relative, "relative");
            relative = Set.copyOf(relative);
        }
    }
}
