package me.imbanana.whatwemissed.block.custom;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class CopperRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    protected final WeatheringCopper.WeatherState weatherState;

    public CopperRailBlock(WeatheringCopper.WeatherState weatherState, Properties properties) {
        this(true, weatherState, properties);
    }

    protected CopperRailBlock(boolean isStraight, WeatheringCopper.WeatherState weatherState, Properties properties) {
        super(isStraight, properties);
        this.weatherState = weatherState;
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, false));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public float getRailFrictionModifier() {
        return switch (this.weatherState) {
            case UNAFFECTED -> 0.4f;
            case EXPOSED -> 0.7f;
            case WEATHERED -> 1.3f;
            case OXIDIZED -> 1.6f;
        };
    }

    @Override
    protected BlockState rotate(final BlockState state, final Rotation rotation) {
        RailShape currentShape = state.getValue(SHAPE);
        RailShape newShape = this.rotate(currentShape, rotation);
        return state.setValue(SHAPE, newShape);
    }

    @Override
    protected BlockState mirror(final BlockState state, final Mirror mirror) {
        RailShape currentShape = state.getValue(SHAPE);
        RailShape newShape = this.mirror(currentShape, mirror);
        return state.setValue(SHAPE, newShape);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }
}
