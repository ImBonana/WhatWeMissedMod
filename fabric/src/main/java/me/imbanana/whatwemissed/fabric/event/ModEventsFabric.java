package me.imbanana.whatwemissed.fabric.event;

import me.imbanana.whatwemissed.block.ModBlocks;
import me.imbanana.whatwemissed.event.ModEvents;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;

public class ModEventsFabric {
    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> ModEvents.modifyDefaultItemComponents((item, modifyConsumer) -> context.modify(item, modifyConsumer::modify)));

        OxidizableBlocksRegistry.registerWeatheringCopperBlocks(ModBlocks.COPPER_RAIL);

        BlockTransformerHelper.registerStripping(ModBlocks.OAK_WALL, ModBlocks.STRIPPED_OAK_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.SPRUCE_WALL, ModBlocks.STRIPPED_SPRUCE_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.BIRCH_WALL, ModBlocks.STRIPPED_BIRCH_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.JUNGLE_WALL, ModBlocks.STRIPPED_JUNGLE_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.ACACIA_WALL, ModBlocks.STRIPPED_ACACIA_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.DARK_OAK_WALL, ModBlocks.STRIPPED_DARK_OAK_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.MANGROVE_WALL, ModBlocks.STRIPPED_MANGROVE_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.CHERRY_WALL, ModBlocks.STRIPPED_CHERRY_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.PALE_OAK_WALL, ModBlocks.STRIPPED_PALE_OAK_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.POPLAR_WALL, ModBlocks.STRIPPED_POPLAR_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.BAMBOO_WALL, ModBlocks.STRIPPED_BAMBOO_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.CRIMSON_STEM_WALL, ModBlocks.STRIPPED_CRIMSON_STEM_WALL);
        BlockTransformerHelper.registerStripping(ModBlocks.WARPED_STEM_WALL, ModBlocks.STRIPPED_WARPED_STEM_WALL);
    }
}
