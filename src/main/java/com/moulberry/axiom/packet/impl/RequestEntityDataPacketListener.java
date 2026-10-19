package com.moulberry.axiom.packet.impl;

import com.moulberry.axiom.AxiomPaper;
import com.moulberry.axiom.VersionHelper;
import com.moulberry.axiom.integration.Integration;
import com.moulberry.axiom.packet.PacketHandler;
import com.moulberry.axiom.nbt.AxiomNbtBridge;
import com.moulberry.axiom.paperapi.entity.AxiomVirtualEntityApi;
import com.moulberry.axiom.restrictions.AxiomPermission;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueOutput;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.*;

public class RequestEntityDataPacketListener implements PacketHandler {

    public static final Identifier RESPONSE_ID = VersionHelper.createIdentifier("axiom:response_entity_data");

    private final AxiomPaper plugin;
    public RequestEntityDataPacketListener(AxiomPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onReceive(org.bukkit.entity.Player bukkitPlayer, FriendlyByteBuf friendlyByteBuf) {
        ServerPlayer player = ((CraftPlayer)bukkitPlayer).getHandle();
        long id = friendlyByteBuf.readLong();

        if (!this.plugin.canUseAxiom(bukkitPlayer, AxiomPermission.ENTITY_REQUESTDATA) || this.plugin.isMismatchedDataVersion(bukkitPlayer.getUniqueId())) {
            // We always send an 'empty' response in order to make the client happy
            sendResponse(player, id, true, Map.of());
            return;
        }

        if (!this.plugin.canModifyWorld(bukkitPlayer, bukkitPlayer.getWorld())) {
            sendResponse(player, id, true, Map.of());
            return;
        }

        List<UUID> request = friendlyByteBuf.readCollection(this.plugin.limitCollection(ArrayList::new), buf -> buf.readUUID());
        ServerLevel serverLevel = player.level();

        final int maxPacketSize = 0x100000;
        int remainingBytes = maxPacketSize;

        Map<UUID, CompoundTag> entityData = new HashMap<>();

        Set<UUID> visitedEntities = new HashSet<>();

        for (UUID uuid : request) {
            if (!visitedEntities.add(uuid)) {
                continue;
            }

            Entity entity = serverLevel.getEntity(uuid);
            if (entity == null) {
                var virtual = AxiomVirtualEntityApi.find(bukkitPlayer.getUniqueId(), uuid);
                if (virtual != null) {
                    var snapshot = virtual.snapshot();
                    Location location = new Location(
                            Bukkit.getWorld(snapshot.worldUuid()),
                            snapshot.x(), snapshot.y(), snapshot.z(),
                            snapshot.yaw(), snapshot.pitch());
                    if (location.getWorld() != null
                            && location.getWorld().equals(bukkitPlayer.getWorld())
                            && Integration.canPlaceBlock(bukkitPlayer, location)) {
                        final Identifier entityTypeKey;
                        try {
                            entityTypeKey = Identifier.parse(snapshot.entityTypeKey());
                        } catch (IllegalArgumentException ignored) {
                            continue;
                        }
                        var entityType = BuiltInRegistries.ENTITY_TYPE.get(entityTypeKey);
                        if (entityType.isEmpty()
                                || !this.plugin.canEntityBeManipulated(entityType.get().value())) {
                            continue;
                        }
                        CompoundTag entityTag = AxiomNbtBridge.toMinecraft(snapshot.entityData());
                        entityTag.putString("id", entityTypeKey.toString());
                        entityTag.putIntArray("UUID", UUIDUtil.uuidToIntArray(uuid));
                        entityTag.put("Pos", new ListTag(List.of(
                                DoubleTag.valueOf(location.getX()),
                                DoubleTag.valueOf(location.getY()),
                                DoubleTag.valueOf(location.getZ())
                        )));
                        entityTag.put("Rotation", new ListTag(List.of(
                                FloatTag.valueOf(location.getYaw()),
                                FloatTag.valueOf(location.getPitch())
                        )));
                        int size = entityTag.sizeInBytes();
                        if (size >= maxPacketSize) {
                            sendResponse(player, id, false, Map.of(uuid, entityTag));
                        } else {
                            if (remainingBytes - size < 0) {
                                sendResponse(player, id, false, entityData);
                                entityData.clear();
                                remainingBytes = maxPacketSize;
                            }
                            entityData.put(uuid, entityTag);
                            remainingBytes -= size;
                        }
                    }
                }
                continue;
            }
            if (entity instanceof Player) {
                continue;
            }

            if (!this.plugin.canEntityBeManipulated(entity.getType())) {
                continue;
            }

            if (!Integration.canPlaceBlock(bukkitPlayer, new Location(bukkitPlayer.getWorld(),
                    entity.getBlockX(), entity.getBlockY(), entity.getBlockZ()))) {
                continue;
            }

            var valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
            var entityTag = entity.save(valueOutput) ? valueOutput.buildResult() : null;
            if (entityTag != null) {
                int size = entityTag.sizeInBytes();
                if (size >= maxPacketSize) {
                    sendResponse(player, id, false, Map.of(uuid, entityTag));
                    continue;
                }

                // Send partial packet if we've run out of available bytes
                if (remainingBytes - size < 0) {
                    sendResponse(player, id, false, entityData);
                    entityData.clear();
                    remainingBytes = maxPacketSize;
                }

                entityData.put(uuid, entityTag);
                remainingBytes -= size;
            }
        }

        sendResponse(player, id, true, entityData);
    }

    private static void sendResponse(ServerPlayer player, long id, boolean finished, Map<UUID, CompoundTag> map) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlyByteBuf.writeLong(id);
        friendlyByteBuf.writeBoolean(finished);
        friendlyByteBuf.writeMap(map, (buf, uuid) -> buf.writeUUID(uuid), (buf, nbt) -> buf.writeNbt(nbt));

        byte[] bytes = ByteBufUtil.getBytes(friendlyByteBuf);
        VersionHelper.sendCustomPayload(player, RESPONSE_ID, bytes);
    }

}
