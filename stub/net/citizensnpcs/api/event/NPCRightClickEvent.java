// 编译桩（说明见 CitizensAPI.java）
package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCRightClickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public NPC getNPC() {
        return null;
    }

    public Player getClicker() {
        return null;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
