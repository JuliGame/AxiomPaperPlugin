package com.moulberry.axiom.paperapi.entity;

import java.util.List;
import java.util.Objects;

public sealed interface AxiomNbtValue permits
        AxiomNbtValue.ByteValue,
        AxiomNbtValue.ShortValue,
        AxiomNbtValue.IntValue,
        AxiomNbtValue.LongValue,
        AxiomNbtValue.FloatValue,
        AxiomNbtValue.DoubleValue,
        AxiomNbtValue.StringValue,
        AxiomNbtValue.ByteArrayValue,
        AxiomNbtValue.IntArrayValue,
        AxiomNbtValue.LongArrayValue,
        AxiomNbtValue.ListValue,
        AxiomNbtCompound {

    record ByteValue(byte value) implements AxiomNbtValue {}
    record ShortValue(short value) implements AxiomNbtValue {}
    record IntValue(int value) implements AxiomNbtValue {}
    record LongValue(long value) implements AxiomNbtValue {}
    record FloatValue(float value) implements AxiomNbtValue {}
    record DoubleValue(double value) implements AxiomNbtValue {}
    record StringValue(String value) implements AxiomNbtValue {
        public StringValue {
            Objects.requireNonNull(value, "value");
        }
    }

    record ByteArrayValue(byte[] value) implements AxiomNbtValue {
        public ByteArrayValue {
            value = value.clone();
        }
        @Override public byte[] value() { return value.clone(); }
    }

    record IntArrayValue(int[] value) implements AxiomNbtValue {
        public IntArrayValue {
            value = value.clone();
        }
        @Override public int[] value() { return value.clone(); }
    }

    record LongArrayValue(long[] value) implements AxiomNbtValue {
        public LongArrayValue {
            value = value.clone();
        }
        @Override public long[] value() { return value.clone(); }
    }

    record ListValue(List<AxiomNbtValue> value) implements AxiomNbtValue {
        public ListValue {
            Objects.requireNonNull(value, "value");
            value = List.copyOf(value);
        }
    }
}
