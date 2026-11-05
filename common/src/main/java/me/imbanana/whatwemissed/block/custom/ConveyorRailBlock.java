package me.imbanana.whatwemissed.block.custom;

import me.imbanana.whatwemissed.util.IDirectionalRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

public class ConveyorRailBlock extends BaseRailBlock implements IDirectionalRailBlock {
    private static final int MAX_LINE_LENGTH = 8;

    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final IntegerProperty SPEED = IntegerProperty.create("speed", 0, 15);

    public ConveyorRailBlock(Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.registerFacingState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, false).setValue(SPEED, 0)));
    }

    @Override
    public RailDirection getRailDirection(BlockState state) {
        if (state.getValue(SPEED) <= 0) return RailDirection.FREESTYLE;

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
    public Vec3 calculateDeltaMovement(Vec3 deltaMovement, BlockState state) {
        Vec3 faceDir = this.getRailDirection(state).getDirection();
        if (faceDir == null) return deltaMovement;

        double speedFactor = 1f / (16 * (16 - state.getValue(SPEED)));
        Vec3 dir = deltaMovement.horizontalDistance() < 1.0E-5
                ? faceDir.multiply(1, 0, 1).add(0, deltaMovement.y(), 0)
                : deltaMovement.normalize();

        return dir.scale(speedFactor);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getPlacementState(super.getStateForPlacement(context), context.getHorizontalDirection());
    }

    @Override
    protected void updateState(BlockState state, Level level, BlockPos pos, Block block) {
        this.realign(this, level, pos);
        this.updateSpeed(level, pos);
    }

    private void updateSpeed(Level level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (!current.is(this)) return;

        RailShape shape = current.getValue(getShapeProperty());
        Direction facing = current.getValue(FACING);
        boolean shapeIsX = shape == RailShape.EAST_WEST || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST;

        Direction a = shapeIsX ? Direction.EAST : Direction.NORTH;

        int speed = level.getBestNeighborSignal(pos);
        speed = Math.max(speed, strongestSignal(level, pos, a, facing));
        speed = Math.max(speed, strongestSignal(level, pos, a.getOpposite(), facing));

        if (speed != current.getValue(SPEED)) {
            level.setBlock(pos, current.setValue(SPEED, speed), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos.below(), this);
            if (shape.isSlope()) level.updateNeighborsAt(pos.above(), this);
        }
    }

    private int strongestSignal(Level level, BlockPos start, Direction dir, Direction facing) {
        int best = 0;
        BlockPos cursor = start;

        for (int i = 0; i < MAX_LINE_LENGTH; i++) {
            BlockPos base = cursor.relative(dir);
            BlockPos next = null;

            for (BlockPos candidate : new BlockPos[]{base, base.above(), base.below()}) {
                BlockState s = level.getBlockState(candidate);
                if (s.getBlock() instanceof ConveyorRailBlock && s.getValue(FACING) == facing) {
                    next = candidate;
                    break;
                }
            }
            if (next == null) break;

            best = Math.max(best, level.getBestNeighborSignal(next));
            if (best == 15) break;
            cursor = next;
        }

        return best;
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public Block asBlock() {
        return super.asBlock();
    }

    @Override
    protected BlockState rotate(final BlockState state, final Rotation rotation) {
        RailShape currentShape = state.getValue(SHAPE);
        RailShape newShape = this.rotate(currentShape, rotation);
        return this.rotateFacing(state.setValue(SHAPE, newShape), rotation);
    }

    @Override
    protected BlockState mirror(final BlockState state, final Mirror mirror) {
        RailShape currentShape = state.getValue(SHAPE);
        RailShape newShape = this.mirror(currentShape, mirror);
        return this.mirrorFacing(state.setValue(SHAPE, newShape), mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED, SPEED);
        this.createDirectionBlockStateDefinition(builder);
    }

    @Override
    protected boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(SPEED);
    }
}
