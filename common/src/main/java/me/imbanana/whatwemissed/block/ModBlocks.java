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

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
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

    public static void init() { }
}
