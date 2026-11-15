// 编译桩：citizensapi 2.0.24 源不可获取（官方库经代理 404、github 仓库为远古分支）。
// 仅满足 MyPet CitizensHook/traits 编译期符号解析；不打入产物（本服无 Citizens，hook 永不启用）。
package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.trait.TraitFactory;

public class CitizensAPI {
    public static boolean hasImplementation() {
        return false;
    }

    public static TraitFactory getTraitFactory() {
        return null;
    }

    public static NPCRegistry getNPCRegistry() {
        return null;
    }

    public static NPCSelector getDefaultNPCSelector() {
        return null;
    }
}
