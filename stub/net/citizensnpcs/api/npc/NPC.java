// 编译桩（说明见 CitizensAPI.java）
package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Entity;

public interface NPC {
    int getId();

    String getName();

    Entity getEntity();

    NPCData data();

    <T extends Trait> T getTrait(Class<T> traitClass);

    <T extends Trait> boolean hasTrait(Class<T> traitClass);

    <T extends Trait> T addTrait(Class<T> traitClass);

    interface NPCData {
        boolean get(String key, boolean def);
    }
}
