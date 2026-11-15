package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.npc.NPC;

public abstract class Trait {
    protected NPC npc;

    protected Trait(String name) {
    }

    public NPC getNPC() {
        return npc;
    }
}
