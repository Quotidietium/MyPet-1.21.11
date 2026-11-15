// 编译桩（说明见 CitizensAPI.java）
package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.trait.Trait;

import java.util.UUID;

public class Owner extends Trait {
    public Owner() {
        super("owner");
    }

    public UUID getOwnerId() {
        return null;
    }
}
