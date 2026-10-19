package com.moulberry.axiom.nbt;

import com.moulberry.axiom.paperapi.entity.AxiomNbtCompound;
import com.moulberry.axiom.paperapi.entity.AxiomNbtValue;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AxiomNbtBridge {
    private AxiomNbtBridge() {
    }

    public static AxiomNbtCompound fromMinecraft(CompoundTag source) {
        Map<String, AxiomNbtValue> values = new LinkedHashMap<>();
        source.forEach((key, value) -> values.put(key, fromMinecraft(value)));
        return new AxiomNbtCompound(values);
    }

    public static CompoundTag toMinecraft(AxiomNbtCompound source) {
        CompoundTag result = new CompoundTag();
        source.values().forEach((key, value) -> result.put(key, toMinecraft(value)));
        return result;
    }

    private static AxiomNbtValue fromMinecraft(Tag tag) {
        return switch (tag) {
            case ByteTag value -> new AxiomNbtValue.ByteValue(value.value());
            case ShortTag value -> new AxiomNbtValue.ShortValue(value.value());
            case IntTag value -> new AxiomNbtValue.IntValue(value.value());
            case LongTag value -> new AxiomNbtValue.LongValue(value.value());
            case FloatTag value -> new AxiomNbtValue.FloatValue(value.value());
            case DoubleTag value -> new AxiomNbtValue.DoubleValue(value.value());
            case StringTag value -> new AxiomNbtValue.StringValue(value.value());
            case ByteArrayTag value -> new AxiomNbtValue.ByteArrayValue(value.getAsByteArray());
            case IntArrayTag value -> new AxiomNbtValue.IntArrayValue(value.getAsIntArray());
            case LongArrayTag value -> new AxiomNbtValue.LongArrayValue(value.getAsLongArray());
            case ListTag value -> {
                List<AxiomNbtValue> entries = new ArrayList<>(value.size());
                for (Tag entry : value) {
                    entries.add(fromMinecraft(entry));
                }
                yield new AxiomNbtValue.ListValue(entries);
            }
            case CompoundTag value -> fromMinecraft(value);
            default -> throw new IllegalArgumentException("Unsupported NBT tag: " + tag.getClass().getName());
        };
    }

    private static Tag toMinecraft(AxiomNbtValue value) {
        return switch (value) {
            case AxiomNbtValue.ByteValue entry -> ByteTag.valueOf(entry.value());
            case AxiomNbtValue.ShortValue entry -> ShortTag.valueOf(entry.value());
            case AxiomNbtValue.IntValue entry -> IntTag.valueOf(entry.value());
            case AxiomNbtValue.LongValue entry -> LongTag.valueOf(entry.value());
            case AxiomNbtValue.FloatValue entry -> FloatTag.valueOf(entry.value());
            case AxiomNbtValue.DoubleValue entry -> DoubleTag.valueOf(entry.value());
            case AxiomNbtValue.StringValue entry -> StringTag.valueOf(entry.value());
            case AxiomNbtValue.ByteArrayValue entry -> new ByteArrayTag(entry.value());
            case AxiomNbtValue.IntArrayValue entry -> new IntArrayTag(entry.value());
            case AxiomNbtValue.LongArrayValue entry -> new LongArrayTag(entry.value());
            case AxiomNbtValue.ListValue entry ->
                    new ListTag(entry.value().stream().map(AxiomNbtBridge::toMinecraft).toList());
            case AxiomNbtCompound entry -> toMinecraft(entry);
        };
    }
}
