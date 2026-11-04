package me.imbanana.whatwemissed.util;

import me.imbanana.whatwemissed.block.custom.DirectorRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public interface IDirectionalRailBlock {
    EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    Property<RailShape> shapeProperty();

    default void realign(Level level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        BlockState fixed = current;

        RailShape shape = current.getValue(shapeProperty());
        boolean shapeIsX = shape == RailShape.EAST_WEST || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST;
        boolean facingIsX = current.getValue(FACING).getAxis() == Direction.Axis.X;
        if (shapeIsX != facingIsX) {
            Direction direction = shapeIsX ? Direction.EAST : Direction.SOUTH;

            BlockPos firstPos = shapeIsX ? pos.east() : pos.north();
            BlockPos secondPos = shapeIsX ? pos.west() : pos.south();

            BlockState sideBlock = level.getBlockState(firstPos);

            if (!(sideBlock.getBlock() instanceof DirectorRailBlock)) {
                sideBlock = level.getBlockState(secondPos);
            }

            if (sideBlock.getBlock() instanceof DirectorRailBlock) {
                direction = sideBlock.getValue(FACING);
            }

            fixed = current.setValue(FACING, direction);
        }

        if (fixed != current) level.setBlockAndUpdate(pos, fixed);
    }

    default BlockState getPlacementState(BlockState base, Direction facing) {
        return base.setValue(FACING, facing).setValue(shapeProperty(), facing.getAxis() == Direction.Axis.X ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH);
    }

    default void createDirectionBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    default BlockState rotateFacing(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    default BlockState mirrorFacing(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    default BlockState registerFacingState(BlockState defaultState) {
        return defaultState.setValue(FACING, Direction.NORTH);
    }
}
