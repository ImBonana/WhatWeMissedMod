package me.imbanana.whatwemissed.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class CustomMinecartBehavior extends NewMinecartBehavior {
    public CustomMinecartBehavior(AbstractMinecart minecart) {
        super(minecart);
    }

    @Override
    public Vec3 calculateTrackSpeed(
            final ServerLevel level,
            final Vec3 deltaMovement,
            final NewMinecartBehavior.TrackIteration trackIteration,
            final BlockPos currentPos,
            final BlockState currentState,
            final RailShape shape
    ) {
        Vec3 newDeltaMovement = deltaMovement;
        if (!trackIteration.hasGainedSlopeSpeed) {
            Vec3 slopedDeltaMovement = this.calculateSlopeSpeed(newDeltaMovement, shape);
            if (slopedDeltaMovement.horizontalDistanceSqr() != newDeltaMovement.horizontalDistanceSqr()) {
                trackIteration.hasGainedSlopeSpeed = true;
                newDeltaMovement = slopedDeltaMovement;
            }
        }

        if (trackIteration.firstIteration) {
            Vec3 playerInputMovement = this.calculatePlayerInputSpeed(newDeltaMovement);
            if (playerInputMovement.horizontalDistanceSqr() != newDeltaMovement.horizontalDistanceSqr()) {
                trackIteration.hasHalted = true;
                newDeltaMovement = playerInputMovement;
            }
        }

        if (!trackIteration.hasHalted) {
            Vec3 haltedDeltaMovement = this.calculateHaltTrackSpeed(newDeltaMovement, currentState);
            if (haltedDeltaMovement.horizontalDistanceSqr() != newDeltaMovement.horizontalDistanceSqr()) {
                trackIteration.hasHalted = true;
                newDeltaMovement = haltedDeltaMovement;
            }
        }

        if (trackIteration.firstIteration) {
            newDeltaMovement = this.minecart.applyNaturalSlowdown(newDeltaMovement);
            if (newDeltaMovement.lengthSqr() > 0.0) {
                double speed = Math.min(newDeltaMovement.length(), this.minecart.getMaxSpeed(level));
                newDeltaMovement = newDeltaMovement.normalize().scale(speed);
            }
        }

        if (trackIteration.firstIteration) {
            Vec3 frictionAppliedDeltaMovement = this.applyFriction(newDeltaMovement, currentState);
            if (frictionAppliedDeltaMovement.horizontalDistanceSqr() != newDeltaMovement.horizontalDistanceSqr()) {
                newDeltaMovement = frictionAppliedDeltaMovement;
            }
        }

        if (!trackIteration.hasBoosted) {
            Vec3 boostedDeltaMovement = this.calculateBoostTrackSpeed(newDeltaMovement, currentPos, currentState);
            if (boostedDeltaMovement.horizontalDistanceSqr() != newDeltaMovement.horizontalDistanceSqr()) {
                trackIteration.hasBoosted = true;
                newDeltaMovement = boostedDeltaMovement;
            }
        }

        return newDeltaMovement;
    }

    private Vec3 applyFriction(Vec3 deltaMovement, BlockState state) {
        float friction = this.minecart.isVehicle() ? 0.003f : 0.125f;

        if (state.getBlock() instanceof IRailBlock railBlock) {
            friction *= railBlock.getRailFrictionModifier();
        }

        return deltaMovement.scale(1 - friction);
    }

    @Override
    public void setRotation(float yRot, float xRot) {
        Vec3 movement = this.getDeltaMovement().horizontal();

        if (movement.lengthSqr() > 1) {
            yRot = MinecartMath.yRotFromMovement(movement);
        } if (Math.abs(Mth.wrapDegrees(yRot - this.getYRot())) > 135.0F) {
            yRot = this.getYRot();
            xRot = -xRot;
        }

        xRot = Math.clamp(xRot, -45.0F, 45.0F);
        this.setXRot(xRot % 360.0F);
        this.setYRot(yRot % 360.0F);
    }

    @Override
    public Vec3 calculateBoostTrackSpeed(final Vec3 deltaMovement, final BlockPos pos, final BlockState state) {
        if (state.is(Blocks.POWERED_RAIL) && state.getValue(PoweredRailBlock.POWERED)) {
            if (deltaMovement.length() > 0.01) {
                return deltaMovement.normalize().scale(deltaMovement.length() + 0.0625);
            }

            Vec3 powerDirection = this.minecart.getRedstoneDirection(pos);
            return powerDirection.lengthSqr() <= 0.0 ? deltaMovement : powerDirection.scale(deltaMovement.length() + 0.2);
        } else {
            return deltaMovement;
        }
    }

    @Override
    public double getSlowdownFactor() {
        return 1f;
    }

    @Override
    public double getMaxSpeed(final ServerLevel level) {
        return 40 * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
    }
}