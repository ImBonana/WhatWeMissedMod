package me.imbanana.whatwemissed.block.custom;

import me.imbanana.whatwemissed.util.IDirectionalRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DirectorRailBlock extends PoweredRailBlock implements IDirectionalRailBlock {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getPlacementState(super.getStateForPlacement(context), context.getHorizontalDirection());
    }

    @Override
    protected void updateState(BlockState state, Level level, BlockPos pos, Block block) {
        boolean wasPowered = state.getValue(POWERED);

        super.updateState(state, level, pos, block);

        BlockState current = level.getBlockState(pos);
        if (!wasPowered && current.is(this) && current.getValue(POWERED)) {
            giveInitialBoost(level, pos, current);
        }

        this.realign(this, level, pos);
    }

    private void giveInitialBoost(Level level, BlockPos pos, BlockState state) {
        Vec3 dir = this.getRailDirection(state).getDirection();
        if (dir == null) return;
        Vec3 flat = dir.multiply(1, 0, 1);
        if (flat.lengthSqr() < 1.0E-10) return;

        for (AbstractMinecart cart : level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos))) {
            if (cart.getDeltaMovement().horizontalDistanceSqr() < 1.0E-3) {
                cart.setDeltaMovement(flat.normalize().scale(0.5).add(0, cart.getDeltaMovement().y(), 0));
            }
        }
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
