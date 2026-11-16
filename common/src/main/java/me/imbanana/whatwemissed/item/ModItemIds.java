package me.imbanana.whatwemissed.item;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.WeatheringCopperCollection;

public class ModItemIds {
    public static final ResourceKey<Item> COPPER_CHEST_MINECART = create("copper_chest_minecart");

    private static ResourceKey<Item> create(String name) {
        return ResourceKey.create(Registries.ITEM, WhatWeMissed.idOf(name));
    }
}
