package me.imbanana.whatwemissed.block.custom;

import me.imbanana.whatwemissed.util.IDirectionalRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;

public class DirectorRailBlock extends PoweredRailBlock implements IDirectionalRailBlock {
    private boolean shouldGiveBoost = false;

    public DirectorRailBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.registerFacingState(this.defaultBlockState()));
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
    public Vec3 calculateDeltaMovement(Vec3 deltaMovement, BlockState state) {
        if (!shouldGiveBoost) return deltaMovement;
        shouldGiveBoost = false;

        Vec3 faceDir = this.getRailDirection(state).getDirection();
        if (faceDir == null) return deltaMovement;

        return deltaMovement.horizontalDistance() < 1.0E-3
                ? faceDir.multiply(1, 0, 1).add(0, deltaMovement.y(), 0).normalize().scale(0.5)
                : super.calculateDeltaMovement(deltaMovement, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getPlacementState(super.getStateForPlacement(context), context.getHorizontalDirection());
    }

    @Override
    protected void updateState(BlockState state, Level level, BlockPos pos, Block block) {
        boolean isPowered = state.getValue(POWERED);

        super.updateState(state, level, pos, block);

        boolean isPoweredAfterUpdate = level.getBlockState(pos).getValue(POWERED);
        if (!isPowered && isPoweredAfterUpdate) {
            shouldGiveBoost = true;
        } else if(isPowered && !isPoweredAfterUpdate) {
            shouldGiveBoost = false;
        }

        this.realign(this, level, pos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return this.rotateFacing(super.rotate(state, rotation), rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return this.mirrorFacing(super.mirror(state, mirror), mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        this.createDirectionBlockStateDefinition(builder);
    }

    // Fixing the neoforge class
    public boolean isActivatorRail() {
        return false;
    }
}
