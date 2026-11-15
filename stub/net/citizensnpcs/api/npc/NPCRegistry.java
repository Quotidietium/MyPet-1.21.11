package net.citizensnpcs.api.npc;

import org.bukkit.entity.Entity;

public interface NPCRegistry {
    NPC getNPC(Entity entity);

    boolean isNPC(Entity entity);
}
