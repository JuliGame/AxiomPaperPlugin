package com.moulberry.axiom.paperapi.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record AxiomNbtCompound(Map<String, AxiomNbtValue> values) implements AxiomNbtValue {
    public static final AxiomNbtCompound EMPTY = new AxiomNbtCompound(Map.of());

    public AxiomNbtCompound {
        Objects.requireNonNull(values, "values");
        values.forEach((key, value) -> {
            Objects.requireNonNull(key, "NBT key");
            Objects.requireNonNull(value, "NBT value for " + key);
        });
        values = Map.copyOf(values);
    }

    public AxiomNbtValue get(String key) {
        return values.get(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, AxiomNbtValue> values = new LinkedHashMap<>();

        public Builder put(String key, AxiomNbtValue value) {
            values.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public Builder putByte(String key, byte value) {
            return put(key, new AxiomNbtValue.ByteValue(value));
        }

        public Builder putInt(String key, int value) {
            return put(key, new AxiomNbtValue.IntValue(value));
        }

        public Builder putFloat(String key, float value) {
            return put(key, new AxiomNbtValue.FloatValue(value));
        }

        public Builder putDouble(String key, double value) {
            return put(key, new AxiomNbtValue.DoubleValue(value));
        }

        public Builder putString(String key, String value) {
            return put(key, new AxiomNbtValue.StringValue(value));
        }

        public AxiomNbtCompound build() {
            return new AxiomNbtCompound(values);
        }
    }
}
