package me.imbanana.whatwemissed.block;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.WeatheringCopperCollection;

public class ModBlockItemIds {
    public static final BlockItemId DEBUG_REDSTONE_SIGNAL = create("redstone_signal");
    public static final WeatheringCopperCollection<BlockItemId> COPPER_RAIL = createSimpleCopper("copper_rail");
    public static final BlockItemId RAIL_JUNCTION = create("rail_junction");
    public static final BlockItemId DIRECTOR_RAIL = create("director_rail");
    public static final BlockItemId CONVEYOR_RAIL = create("conveyor_rail");
    public static final BlockItemId TACKY_PISTON = create("tacky_piston");
    public static final BlockItemId OAK_WALL = create("oak_wall");
    public static final BlockItemId STRIPPED_OAK_WALL = create("stripped_oak_wall");
    public static final BlockItemId SPRUCE_WALL = create("spruce_wall");
    public static final BlockItemId STRIPPED_SPRUCE_WALL = create("stripped_spruce_wall");
    public static final BlockItemId BIRCH_WALL = create("birch_wall");
    public static final BlockItemId STRIPPED_BIRCH_WALL = create("stripped_birch_wall");
    public static final BlockItemId JUNGLE_WALL = create("jungle_wall");
    public static final BlockItemId STRIPPED_JUNGLE_WALL = create("stripped_jungle_wall");
    public static final BlockItemId ACACIA_WALL = create("acacia_wall");
    public static final BlockItemId STRIPPED_ACACIA_WALL = create("stripped_acacia_wall");
    public static final BlockItemId DARK_OAK_WALL = create("dark_oak_wall");
    public static final BlockItemId STRIPPED_DARK_OAK_WALL = create("stripped_dark_oak_wall");
    public static final BlockItemId MANGROVE_WALL = create("mangrove_wall");
    public static final BlockItemId STRIPPED_MANGROVE_WALL = create("stripped_mangrove_wall");
    public static final BlockItemId CHERRY_WALL = create("cherry_wall");
    public static final BlockItemId STRIPPED_CHERRY_WALL = create("stripped_cherry_wall");
    public static final BlockItemId PALE_OAK_WALL = create("pale_oak_wall");
    public static final BlockItemId STRIPPED_PALE_OAK_WALL = create("stripped_pale_oak_wall");
    public static final BlockItemId POPLAR_WALL = create("poplar_wall");
    public static final BlockItemId STRIPPED_POPLAR_WALL = create("stripped_poplar_wall");
    public static final BlockItemId BAMBOO_WALL = create("bamboo_wall");
    public static final BlockItemId STRIPPED_BAMBOO_WALL = create("stripped_bamboo_wall");
    public static final BlockItemId CRIMSON_STEM_WALL = create("crimson_stem_wall");
    public static final BlockItemId STRIPPED_CRIMSON_STEM_WALL = create("stripped_crimson_stem_wall");
    public static final BlockItemId WARPED_STEM_WALL = create("warped_stem_wall");
    public static final BlockItemId STRIPPED_WARPED_STEM_WALL = create("stripped_warped_stem_wall");

    private static BlockItemId create(String name) {
        Identifier id = WhatWeMissed.idOf(name);
        return BlockItemId.create(id, id);
    }

    private static WeatheringCopperCollection<BlockItemId> createSimpleCopper(final String baseName) {
        return WeatheringCopperCollection.prefixWithState(WeatheringCopperCollection.create(baseName)).map(ModBlockItemIds::create);
    }
}
