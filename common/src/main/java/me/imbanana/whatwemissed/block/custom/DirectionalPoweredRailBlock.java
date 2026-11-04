package me.imbanana.whatwemissed.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;

public class DirectionalPoweredRailBlock extends PoweredRailBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DirectionalPoweredRailBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RailDirection getRailDirection(BlockState state) {
        if (!state.getValue(POWERED)) return RailDirection.FREESTYLE;

        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case NORTH -> RailDirection.NORTH;
            case SOUTH -> RailDirection.SOUTH;
            case EAST -> RailDirection.EAST;
            case WEST -> RailDirection.WEST;
            default -> RailDirection.FREESTYLE;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState base = super.getStateForPlacement(context);
        Direction facing = context.getHorizontalDirection();
        return base.setValue(FACING, facing).setValue(SHAPE, facing.getAxis() == Direction.Axis.X ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH);
    }

    @Override
    protected void updateState(BlockState state, Level level, BlockPos pos, Block block) {
        super.updateState(state, level, pos, block);

        BlockState current = level.getBlockState(pos);
        if (current.is(this)) {
            BlockState fixed = current;

            RailShape shape = current.getValue(SHAPE);
            boolean shapeIsX = shape == RailShape.EAST_WEST || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST;
            boolean facingIsX = current.getValue(FACING).getAxis() == Direction.Axis.X;
            if (shapeIsX != facingIsX) {
                Direction direction = shapeIsX ? Direction.EAST : Direction.SOUTH;

                BlockPos firstPos = shapeIsX ? pos.east() : pos.north();
                BlockPos secondPos = shapeIsX ? pos.west() : pos.south();

                BlockState sideBlock = level.getBlockState(firstPos);

                if (!(sideBlock.getBlock() instanceof DirectionalPoweredRailBlock)) {
                    sideBlock = level.getBlockState(secondPos);
                }

                if (sideBlock.getBlock() instanceof DirectionalPoweredRailBlock) {
                    direction = sideBlock.getValue(FACING);
                }

                fixed = current.setValue(FACING, direction);
            }

            if (fixed != current) level.setBlockAndUpdate(pos, fixed);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation).setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return super.mirror(state, mirror).setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
