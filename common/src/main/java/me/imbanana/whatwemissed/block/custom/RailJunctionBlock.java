package me.imbanana.whatwemissed.block.custom;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class RailJunctionBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = EnumProperty.create(
            "shape",
            RailShape.class,
            railShape -> railShape == RailShape.EAST_WEST || railShape == RailShape.NORTH_SOUTH
    );;

    public RailJunctionBlock(Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, false));
    }

    @Override
    public Pair<Vec3i, Vec3i> getRailExits(Vec3 deltaMovement, RailShape shape) {
        Vec3 vel = deltaMovement.horizontal();
        boolean isNorthSouth = vel.lengthSqr() < 1.0E-6
                ? shape == RailShape.NORTH_SOUTH
                : Math.abs(vel.z()) > Math.abs(vel.x());
        return AbstractMinecart.exits(isNorthSouth ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
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
