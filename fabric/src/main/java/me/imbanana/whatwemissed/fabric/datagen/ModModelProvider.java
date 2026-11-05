package me.imbanana.whatwemissed.fabric.datagen;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.block.ModBlocks;
import me.imbanana.whatwemissed.block.custom.ConveyorRailBlock;
import me.imbanana.whatwemissed.block.custom.RailJunctionBlock;
import me.imbanana.whatwemissed.util.IDirectionalRailBlock;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {
    private static final ModelTemplate RAIL_RAISED_NE_FLIPPED = flippedRailTemplate("template_rail_raised_ne_flipped", "_raised_ne_flipped");
    private static final ModelTemplate RAIL_RAISED_SW_FLIPPED = flippedRailTemplate("template_rail_raised_sw_flipped", "_raised_sw_flipped");

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        ModBlocks.COPPER_RAIL.zipUnwaxedWaxed((unwaxed, waxed) -> createCopperRail(blockModelGenerators, unwaxed, waxed));
        createRailJunction(blockModelGenerators, ModBlocks.RAIL_JUNCTION);
        createDirectionalActiveRail(blockModelGenerators, ModBlocks.DIRECTOR_RAIL);
        createConveyorRail(blockModelGenerators, ModBlocks.CONVEYOR_RAIL);
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

    private static void createDirectionalActiveRail(BlockModelGenerators blockModelGenerators, Block block) {
        MultiVariant flat = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "", ModelTemplates.RAIL_FLAT, TextureMapping::rail));
        MultiVariant risingNE = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail));
        MultiVariant risingSW = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail));
        MultiVariant flatOn = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_FLAT, TextureMapping::rail));
        MultiVariant risingNEOn = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail));
        MultiVariant risingSWOn = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail));
        MultiVariant risingNEFlip = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "", RAIL_RAISED_NE_FLIPPED, TextureMapping::rail));
        MultiVariant risingSWFlip = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "", RAIL_RAISED_SW_FLIPPED, TextureMapping::rail));
        MultiVariant risingNEOnFlip = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "_on", RAIL_RAISED_NE_FLIPPED, TextureMapping::rail));
        MultiVariant risingSWOnFlip = BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "_on", RAIL_RAISED_SW_FLIPPED, TextureMapping::rail));

        blockModelGenerators.registerSimpleFlatItemModel(block);
        blockModelGenerators.blockStateOutput
                .accept(
                        MultiVariantGenerator.dispatch(block)
                                .with(PropertyDispatch.initial(
                                        BlockStateProperties.POWERED,
                                        BlockStateProperties.RAIL_SHAPE_STRAIGHT,
                                        IDirectionalRailBlock.FACING
                                ).generate((powered, railShape, facing) -> {
                                    boolean flip = switch (railShape) {
                                        case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> facing == Direction.SOUTH;
                                        default -> facing == Direction.WEST;
                                    };

                                    MultiVariant ne = powered ? (flip ? risingNEOnFlip : risingNEOn) : (flip ? risingNEFlip : risingNE);
                                    MultiVariant sw = powered ? (flip ? risingSWOnFlip : risingSWOn) : (flip ? risingSWFlip : risingSW);

                                    return switch (railShape) {
                                        case NORTH_SOUTH, EAST_WEST -> rotate(powered ? flatOn : flat, facing);
                                        case ASCENDING_NORTH -> ne;
                                        case ASCENDING_SOUTH -> sw;
                                        case ASCENDING_EAST -> ne.with(BlockModelGenerators.Y_ROT_90);
                                        case ASCENDING_WEST -> sw.with(BlockModelGenerators.Y_ROT_90);
                                        default -> throw new UnsupportedOperationException("Fix you generator!");
                                    };
                                }))
                );
    }

    private static void createConveyorRail(BlockModelGenerators blockModelGenerators, Block block) {
        blockModelGenerators.registerSimpleItemModel(block, blockModelGenerators.createFlatItemModel(block.asItem()));

        Map<Integer, Map<DirectionalRailStates, MultiVariant>> speedVariants = new HashMap<>();

        for (Integer speed : ConveyorRailBlock.SPEED.getPossibleValues()) {
            Map<DirectionalRailStates, MultiVariant> variantMap = new HashMap<>();

            variantMap.put(DirectionalRailStates.FLAT, getConveyorRailVariant(blockModelGenerators, block, ModelTemplates.RAIL_FLAT, speed));
            variantMap.put(DirectionalRailStates.RISING_NE, getConveyorRailVariant(blockModelGenerators, block, ModelTemplates.RAIL_RAISED_NE, speed));
            variantMap.put(DirectionalRailStates.RISING_SW, getConveyorRailVariant(blockModelGenerators, block, ModelTemplates.RAIL_RAISED_SW, speed));
            variantMap.put(DirectionalRailStates.RISING_NE_FLIPPED, getConveyorRailVariant(blockModelGenerators, block, RAIL_RAISED_NE_FLIPPED, speed));
            variantMap.put(DirectionalRailStates.RISING_SW_FLIPPED, getConveyorRailVariant(blockModelGenerators, block, RAIL_RAISED_SW_FLIPPED, speed));

            speedVariants.put(speed, variantMap);
        }

        blockModelGenerators.blockStateOutput
                .accept(
                        MultiVariantGenerator.dispatch(block)
                                .with(PropertyDispatch.initial(
                                        ConveyorRailBlock.SPEED,
                                        BlockStateProperties.RAIL_SHAPE_STRAIGHT,
                                        IDirectionalRailBlock.FACING
                                ).generate((speed, railShape, facing) -> {
                                    Map<DirectionalRailStates, MultiVariant> variantMap = speedVariants.get(speed);
                                    MultiVariant flat = variantMap.get(DirectionalRailStates.FLAT);
                                    MultiVariant risingNE = variantMap.get(DirectionalRailStates.RISING_NE);
                                    MultiVariant risingSW = variantMap.get(DirectionalRailStates.RISING_SW);
                                    MultiVariant risingNEFlip = variantMap.get(DirectionalRailStates.RISING_NE_FLIPPED);
                                    MultiVariant risingSWFlip = variantMap.get(DirectionalRailStates.RISING_SW_FLIPPED);

                                    boolean flip = switch (railShape) {
                                        case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> facing == Direction.SOUTH;
                                        default -> facing == Direction.WEST;
                                    };

                                    MultiVariant ne = flip ? risingNEFlip : risingNE;
                                    MultiVariant sw = flip ? risingSWFlip : risingSW;

                                    return switch (railShape) {
                                        case NORTH_SOUTH, EAST_WEST -> rotate(flat, facing);
                                        case ASCENDING_NORTH -> ne;
                                        case ASCENDING_SOUTH -> sw;
                                        case ASCENDING_EAST -> ne.with(BlockModelGenerators.Y_ROT_90);
                                        case ASCENDING_WEST -> sw.with(BlockModelGenerators.Y_ROT_90);
                                        default -> throw new UnsupportedOperationException("Fix you generator!");
                                    };
                                }))
                );
    }

    private static MultiVariant rotate(MultiVariant model, Direction facing) {
        return switch (facing) {
            case EAST -> model.with(BlockModelGenerators.Y_ROT_90);
            case SOUTH -> model.with(BlockModelGenerators.Y_ROT_180);
            case WEST -> model.with(BlockModelGenerators.Y_ROT_270);
            default -> model;
        };
    }

    private static ModelTemplate flippedRailTemplate(String name, String suffix) {
        return new ModelTemplate(
                Optional.of(WhatWeMissed.idOf("block/" + name)),
                Optional.of(suffix),
                TextureSlot.RAIL
        );
    }

    private static MultiVariant getConveyorRailVariant(BlockModelGenerators blockModelGenerators, Block block, ModelTemplate template, int speed) {
        return BlockModelGenerators.plainVariant(blockModelGenerators.createSuffixedVariant(block, "_" + speed, template, material -> TextureMapping.rail(new Material(WhatWeMissed.idOf("block/conveyor_rail_" + speed)))));
    }

    private enum DirectionalRailStates {
        FLAT,
        RISING_NE,
        RISING_SW,
        RISING_NE_FLIPPED,
        RISING_SW_FLIPPED
    }
}
