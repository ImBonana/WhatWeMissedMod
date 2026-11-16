package me.imbanana.whatwemissed.entity;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

public class ModEntityTypeIds {
    public static ResourceKey<EntityType<?>> COPPER_CHEST_MINECART = create("copper_chest_minecart");

    private static ResourceKey<EntityType<?>> create(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, WhatWeMissed.idOf(name));
    }
}
