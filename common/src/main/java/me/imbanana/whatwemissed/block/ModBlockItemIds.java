package me.imbanana.whatwemissed.block;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.WeatheringCopperCollection;

public class ModBlockItemIds {
    public static final WeatheringCopperCollection<BlockItemId> COPPER_RAIL = createSimpleCopper("copper_rail");
    public static final BlockItemId RAIL_JUNCTION = create("rail_junction");
    public static final BlockItemId DIRECTOR_RAIL = create("director_rail");
    public static final BlockItemId CONVEYOR_RAIL = create("conveyor_rail");

    private static BlockItemId create(String name) {
        Identifier id = WhatWeMissed.idOf(name);
        return BlockItemId.create(id, id);
    }

    private static WeatheringCopperCollection<BlockItemId> createSimpleCopper(final String baseName) {
        return WeatheringCopperCollection.prefixWithState(WeatheringCopperCollection.create(baseName)).map(ModBlockItemIds::create);
    }
}
