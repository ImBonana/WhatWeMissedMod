package me.imbanana.whatwemissed.fabric.datagen;

import me.imbanana.whatwemissed.block.ModBlocks;
import me.imbanana.whatwemissed.block.custom.RailJunctionBlock;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        ModBlocks.COPPER_RAIL.zipUnwaxedWaxed((unwaxed, waxed) -> createCopperRail(blockModelGenerators, unwaxed, waxed));
        createRailJunction(blockModelGenerators, ModBlocks.RAIL_JUNCTION);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

    }

    @Override
    public String getName() {
        return "ModModelProvider";
    }

    private static void createCopperRail(BlockModelGenerators blockModelGenerators, Block unwaxed, Block waxed) {
        TextureMapping texture = TextureMapping.rail(unwaxed);
        MultiVariant flat = BlockModelGenerators.plainVariant(ModelTemplates.RAIL_FLAT.create(unwaxed, texture, blockModelGenerators.modelOutput));
        MultiVariant risingNE = BlockModelGenerators.plainVariant(ModelTemplates.RAIL_RAISED_NE.create(unwaxed, texture, blockModelGenerators.modelOutput));
        MultiVariant risingSW = BlockModelGenerators.plainVariant(ModelTemplates.RAIL_RAISED_SW.create(unwaxed, texture, blockModelGenerators.modelOutput));

        PropertyDispatch<MultiVariant> states = PropertyDispatch.initial(BlockStateProperties.RAIL_SHAPE_STRAIGHT)
                .select(RailShape.NORTH_SOUTH, flat)
                .select(RailShape.EAST_WEST, flat.with(BlockModelGenerators.Y_ROT_90))
                .select(RailShape.ASCENDING_EAST, risingNE.with(BlockModelGenerators.Y_ROT_90))
                .select(RailShape.ASCENDING_WEST, risingSW.with(BlockModelGenerators.Y_ROT_90))
                .select(RailShape.ASCENDING_NORTH, risingNE)
                .select(RailShape.ASCENDING_SOUTH, risingSW);

        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(unwaxed).with(states));
        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(waxed).with(states));

        blockModelGenerators.registerSimpleFlatItemModel(unwaxed);
        blockModelGenerators.itemModelOutput.copy(unwaxed.asItem(), waxed.asItem());
    }

    private static void createRailJunction(BlockModelGenerators blockModelGenerators, Block block) {
        TextureMapping texture = TextureMapping.rail(block);
        MultiVariant flat = BlockModelGenerators.plainVariant(ModelTemplates.RAIL_FLAT.create(block, texture, blockModelGenerators.modelOutput));

        PropertyDispatch<MultiVariant> states = PropertyDispatch.initial(RailJunctionBlock.SHAPE)
                .select(RailShape.NORTH_SOUTH, flat)
                .select(RailShape.EAST_WEST, flat.with(BlockModelGenerators.Y_ROT_90));

        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(states));

        blockModelGenerators.registerSimpleFlatItemModel(block);
    }
}
