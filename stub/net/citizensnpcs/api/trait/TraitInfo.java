package net.citizensnpcs.api.trait;

public class TraitInfo {
    public static TraitInfo create(Class<?> traitClass) {
        return new TraitInfo();
    }

    public TraitInfo withName(String name) {
        return this;
    }
}
