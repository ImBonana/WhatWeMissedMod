package me.imbanana.whatwemissed.block;

import me.imbanana.whatwemissed.block.custom.*;
import me.imbanana.whatwemissed.registry.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    private static final Map<Block, Block> WOOD_WALLS = new LinkedHashMap<>();

    public static final Block DEBUG_REDSTONE_SIGNAL = registerWithItem(ModBlockItemIds.DEBUG_REDSTONE_SIGNAL, RedstoneSignalBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.FIRE)
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL)
            .isRedstoneConductor((state, level, pos) -> false)
    );

    public static final WeatheringCopperCollection<Block> COPPER_RAIL = WeatheringCopperCollection.registerBlocks(
            ModBlockItemIds.COPPER_RAIL, ModBlocks::registerWithItem, CopperRailBlock::new, WeatheringCopperRail::new, state -> {
                BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
                MapColor mapColor = switch (state) {
                    case UNAFFECTED -> MapColor.COLOR_ORANGE;
                    case EXPOSED -> MapColor.TERRACOTTA_LIGHT_GRAY;
                    case WEATHERED -> MapColor.WARPED_STEM;
                    case OXIDIZED -> MapColor.WARPED_NYLIUM;
                };

                return properties
                        .mapColor(mapColor)
                        .noCollision()
                        .strength(0.7f)
                        .sound(SoundType.COPPER);
            }
    );
    public static final Block RAIL_JUNCTION = registerWithItem(
            ModBlockItemIds.RAIL_JUNCTION, RailJunctionBlock::new, BlockBehaviour.Properties.of().noCollision().strength(0.7F).sound(SoundType.METAL)
    );
    public static final Block DIRECTOR_RAIL = registerWithItem(
            ModBlockItemIds.DIRECTOR_RAIL, DirectorRailBlock::new, BlockBehaviour.Properties.of().noCollision().strength(0.7F).sound(SoundType.METAL)
    );
    public static final Block CONVEYOR_RAIL = registerWithItem(
            ModBlockItemIds.CONVEYOR_RAIL, ConveyorRailBlock::new, BlockBehaviour.Properties.of().noCollision().strength(0.7F).sound(SoundType.METAL)
    );
    public static final Block TACKY_PISTON = registerWithItem(ModBlockItemIds.TACKY_PISTON, TackyPistonBlock::new, Blocks.pistonProperties());
    public static final Block OAK_WALL = registerWoodWall(ModBlockItemIds.OAK_WALL, Blocks.OAK_LOG);
    public static final Block STRIPPED_OAK_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_OAK_WALL, Blocks.STRIPPED_OAK_LOG);
    public static final Block SPRUCE_WALL = registerWoodWall(ModBlockItemIds.SPRUCE_WALL, Blocks.SPRUCE_LOG);
    public static final Block STRIPPED_SPRUCE_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_SPRUCE_WALL, Blocks.STRIPPED_SPRUCE_LOG);
    public static final Block BIRCH_WALL = registerWoodWall(ModBlockItemIds.BIRCH_WALL, Blocks.BIRCH_LOG);
    public static final Block STRIPPED_BIRCH_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_BIRCH_WALL, Blocks.STRIPPED_BIRCH_LOG);
    public static final Block JUNGLE_WALL = registerWoodWall(ModBlockItemIds.JUNGLE_WALL, Blocks.JUNGLE_LOG);
    public static final Block STRIPPED_JUNGLE_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_JUNGLE_WALL, Blocks.STRIPPED_JUNGLE_LOG);
    public static final Block ACACIA_WALL = registerWoodWall(ModBlockItemIds.ACACIA_WALL, Blocks.ACACIA_LOG);
    public static final Block STRIPPED_ACACIA_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_ACACIA_WALL, Blocks.STRIPPED_ACACIA_LOG);
    public static final Block DARK_OAK_WALL = registerWoodWall(ModBlockItemIds.DARK_OAK_WALL, Blocks.DARK_OAK_LOG);
    public static final Block STRIPPED_DARK_OAK_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_DARK_OAK_WALL, Blocks.STRIPPED_DARK_OAK_LOG);
    public static final Block MANGROVE_WALL = registerWoodWall(ModBlockItemIds.MANGROVE_WALL, Blocks.MANGROVE_LOG);
    public static final Block STRIPPED_MANGROVE_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_MANGROVE_WALL, Blocks.STRIPPED_MANGROVE_LOG);
    public static final Block CHERRY_WALL = registerWoodWall(ModBlockItemIds.CHERRY_WALL, Blocks.CHERRY_LOG);
    public static final Block STRIPPED_CHERRY_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_CHERRY_WALL, Blocks.STRIPPED_CHERRY_LOG);
    public static final Block PALE_OAK_WALL = registerWoodWall(ModBlockItemIds.PALE_OAK_WALL, Blocks.PALE_OAK_LOG);
    public static final Block STRIPPED_PALE_OAK_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_PALE_OAK_WALL, Blocks.STRIPPED_PALE_OAK_LOG);
    public static final Block POPLAR_WALL = registerWoodWall(ModBlockItemIds.POPLAR_WALL, Blocks.POPLAR_LOG);
    public static final Block STRIPPED_POPLAR_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_POPLAR_WALL, Blocks.STRIPPED_POPLAR_LOG);
    public static final Block BAMBOO_WALL = registerWoodWall(ModBlockItemIds.BAMBOO_WALL, Blocks.BAMBOO_BLOCK);
    public static final Block STRIPPED_BAMBOO_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_BAMBOO_WALL, Blocks.STRIPPED_BAMBOO_BLOCK);
    public static final Block CRIMSON_STEM_WALL = registerWoodWall(ModBlockItemIds.CRIMSON_STEM_WALL, Blocks.CRIMSON_STEM);
    public static final Block STRIPPED_CRIMSON_STEM_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_CRIMSON_STEM_WALL, Blocks.STRIPPED_CRIMSON_STEM);
    public static final Block WARPED_STEM_WALL = registerWoodWall(ModBlockItemIds.WARPED_STEM_WALL, Blocks.WARPED_STEM);
    public static final Block STRIPPED_WARPED_STEM_WALL = registerWoodWall(ModBlockItemIds.STRIPPED_WARPED_STEM_WALL, Blocks.STRIPPED_WARPED_STEM);


    private static Supplier<Block> register(ResourceKey<Block> id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        return ModRegistry.register(BuiltInRegistries.BLOCK, id, () -> factory.apply(properties.setId(id)));
    }

    private static Block registerWithItem(BlockItemId id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = factory.apply(properties.setId(id.block()));

        ModRegistry.register(BuiltInRegistries.BLOCK, id.block(), () -> block);
        ModRegistry.register(BuiltInRegistries.ITEM, id.item(), () ->
                new BlockItem(
                        block,
                        new Item.Properties()
                                .useBlockDescriptionPrefix()
                                .setId(id.item())
                )
        );

        return block;
    }

    private static Block registerWoodWall(final BlockItemId id, final Block base) {
        Block wall = registerWall(id, base);
        WOOD_WALLS.put(wall, base);
        return wall;
    }

    private static Block registerWall(final BlockItemId id, final Block base) {
        return registerWithItem(id, WallBlock::new, BlockBehaviour.Properties.ofFullCopy(base).mapColor(base.defaultMapColor()).forceSolidOn());
    }

    public static Map<Block, Block> woodWalls() {
        return Collections.unmodifiableMap(WOOD_WALLS);
    }

    public static void init() { }
}
