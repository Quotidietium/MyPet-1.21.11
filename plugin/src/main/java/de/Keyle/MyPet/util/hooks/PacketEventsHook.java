/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2019 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.util.hooks;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.entity.MyPetBukkitPart;
import de.Keyle.MyPet.api.entity.MyPetType;
import de.Keyle.MyPet.api.util.hooks.PluginHook;
import de.Keyle.MyPet.api.util.hooks.PluginHookName;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * ProtocolLibHook 的 packetevents 等价实现（Quotidietium fork）：
 * - 末影龙宠物交互修正（USE_ENTITY 目标实体 id 改写到宠物主体）
 * - 点击限流（2 tick/玩家，防网络风暴）
 * - 末影龙宠物朝向修正（实体移动/看向/传送包 yaw 偏移 +127 字节角度）
 */
@PluginHookName("packetevents")
public class PacketEventsHook implements PluginHook {

    /** 与原 ProtocolLib 字节实现等价的角度偏移：127 * (360 / 256) 字节角度转浮点度数。 */
    private static final float DRAGON_YAW_OFFSET_DEGREES = 127f * 360f / 256f;

    private final java.util.Set<Player> tempBlockedPlayers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private PacketListenerAbstract interactionListener;
    private PacketListenerAbstract rotationListener;

    @Override
    public boolean onEnable() {
        try {
            registerEnderDragonInteractionFix();
            registerEnderDragonRotationFix();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        try {
            if (interactionListener != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(interactionListener);
            }
            if (rotationListener != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(rotationListener);
            }
        } catch (Exception ignored) {
        }
    }

    private void registerEnderDragonInteractionFix() {
        interactionListener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
                    return;
                }
                Player player = (Player) event.getPlayer();
                if (player == null || event.isCancelled()) {
                    return;
                }

                // 与原实现一致的点击限流：每玩家 2 tick 内的重复交互直接放行原文（不触发主线程解析）
                if (tempBlockedPlayers.contains(player)) {
                    return;
                }
                tempBlockedPlayers.add(player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(MyPetApi.getPlugin(), () -> tempBlockedPlayers.remove(player), 2L);

                WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
                int id = packet.getEntityId();
                try {
                    Entity ent = ensureMainThread(() -> {
                        Entity entity = MyPetApi.getPlatformHelper().getEntity(id, player.getWorld());
                        if (entity instanceof MyPetBukkitPart) {
                            entity = ((MyPetBukkitPart) entity).getPetOwner();
                        }
                        return entity;
                    });
                    if (ent != null) {
                        packet.setEntityId(ent.getEntityId());
                    }
                } catch (TimeoutException e) {
                    // 主线程阻塞时放行 netty 线程（与原实现一致）
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener(interactionListener);
    }

    private void registerEnderDragonRotationFix() {
        rotationListener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (event.isCancelled()) {
                    return;
                }
                if (event.getPacketType() == PacketType.Play.Server.ENTITY_ROTATION
                        || event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                    WrapperPlayServerEntityRotation packet = new WrapperPlayServerEntityRotation(event);
                    if (isEnderDragonPet(packet.getEntityId(), event)) {
                        packet.setYaw(adjustYaw(packet.getYaw()));
                    }
                } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_HEAD_LOOK) {
                    WrapperPlayServerEntityHeadLook packet = new WrapperPlayServerEntityHeadLook(event);
                    if (isEnderDragonPet(packet.getEntityId(), event)) {
                        packet.setHeadYaw(adjustYaw(packet.getHeadYaw()));
                    }
                } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                    WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event);
                    if (isEnderDragonPet(packet.getEntityId(), event)) {
                        packet.setYaw(adjustYaw(packet.getYaw()));
                    }
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener(rotationListener);
    }

    private boolean isEnderDragonPet(int entityId, PacketSendEvent event) {
        if (Bukkit.isPrimaryThread()) {
            return isEnderDragonPetSync(entityId, event);
        }
        try {
            Entity entity = ensureMainThread(() -> {
                Entity e = null;
                try {
                    e = MyPetApi.getPlatformHelper().getEntity(entityId, ((Player) event.getPlayer()).getWorld());
                } catch (Exception ignored) {
                }
                return e;
            });
            return entity instanceof MyPetBukkitEntity && ((MyPetBukkitEntity) entity).getPetType() == MyPetType.EnderDragon;
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            return false;
        }
    }

    private boolean isEnderDragonPetSync(int entityId, PacketSendEvent event) {
        try {
            Entity entity = MyPetApi.getPlatformHelper().getEntity(entityId, ((Player) event.getPlayer()).getWorld());
            return entity instanceof MyPetBukkitEntity && ((MyPetBukkitEntity) entity).getPetType() == MyPetType.EnderDragon;
        } catch (Exception ignored) {
            return false;
        }
    }

    private float adjustYaw(float yaw) {
        return (yaw + DRAGON_YAW_OFFSET_DEGREES) % 360f;
    }

    private <T> T ensureMainThread(Supplier<T> supplier) throws ExecutionException, InterruptedException, TimeoutException {
        if (Bukkit.isPrimaryThread()) {
            return supplier.get();
        }
        return Bukkit.getServer().getScheduler().callSyncMethod(MyPetApi.getPlugin(), supplier::get)
                .get(100, TimeUnit.MILLISECONDS);
    }
}
