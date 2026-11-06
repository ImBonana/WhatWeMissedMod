package me.imbanana.whatwemissed.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
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
    public void adjustToRails(final BlockPos targetBlockPos, final BlockState currentState, final boolean instant) {
        if (BaseRailBlock.isRail(currentState)) {
            RailShape shape = currentState.getValue(((BaseRailBlock)currentState.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> exits = ((BaseRailBlock) currentState.getBlock()).getRailExits(this.getDeltaMovement(), shape);
            Vec3 exit0 = new Vec3(exits.getFirst()).scale(0.5);
            Vec3 exit1 = new Vec3(exits.getSecond()).scale(0.5);
            Vec3 horizontalOutDirection = exit0.horizontal();
            Vec3 horizontalInDirection = exit1.horizontal();
            if (this.getDeltaMovement().length() > 1.0E-5F && this.getDeltaMovement().dot(horizontalOutDirection) < this.getDeltaMovement().dot(horizontalInDirection)
                    || this.isDecending(horizontalInDirection, shape)) {
                Vec3 swap = horizontalOutDirection;
                horizontalOutDirection = horizontalInDirection;
                horizontalInDirection = swap;
            }

            float yRot = 180.0F - (float)(Math.atan2(horizontalOutDirection.z, horizontalOutDirection.x) * 180.0 / Math.PI);
            yRot += this.minecart.isFlipped() ? 180.0F : 0.0F;
            Vec3 previousPosition = this.position();
            boolean inCorner = exit0.x() != exit1.x() && exit0.z() != exit1.z();
            Vec3 targetPosition;
            if (inCorner) {
                Vec3 from0to1 = exit1.subtract(exit0);
                Vec3 from0toPos = previousPosition.subtract(Vec3.atBottomCenterOf(targetBlockPos)).subtract(exit0);
                Vec3 travelVectorFrom0 = from0to1.scale(from0to1.dot(from0toPos) / from0to1.dot(from0to1));
                targetPosition = Vec3.atBottomCenterOf(targetBlockPos).add(exit0).add(travelVectorFrom0);
                yRot = 180.0F - (float)(Math.atan2(travelVectorFrom0.z, travelVectorFrom0.x) * 180.0 / Math.PI);
                yRot += this.minecart.isFlipped() ? 180.0F : 0.0F;
            } else {
                boolean zSnap = exit0.subtract(exit1).x != 0.0;
                boolean xSnap = exit0.subtract(exit1).z != 0.0;
                targetPosition = new Vec3(
                        xSnap ? Vec3.atCenterOf(targetBlockPos).x : previousPosition.x, targetBlockPos.getY(), zSnap ? Vec3.atCenterOf(targetBlockPos).z : previousPosition.z
                );
            }

            Vec3 diffFromBlock = targetPosition.subtract(previousPosition);
            this.setPos(previousPosition.add(diffFromBlock));
            float xRot = 0.0F;
            boolean inHill = exit0.y() != exit1.y();
            if (inHill) {
                Vec3 inPosition = Vec3.atBottomCenterOf(targetBlockPos).add(horizontalInDirection);
                double horizontalDistanceFromIn = inPosition.distanceTo(this.position());
                this.setPos(this.position().add(0.0, horizontalDistanceFromIn + 0.1, 0.0));
                xRot = this.minecart.isFlipped() ? 45.0F : -45.0F;
            } else {
                this.setPos(this.position().add(0.0, 0.1, 0.0));
            }

            this.setRotation(yRot, xRot);
            double adjustDistance = previousPosition.distanceTo(this.position());
            if (adjustDistance > 0.0) {
                float weight = instant ? 0.0F : (float) Math.min(adjustDistance, this.getMaxSpeed((ServerLevel) level()));
                this.lerpSteps
                        .add(
                                new NewMinecartBehavior.MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), weight)
                        );
            }
        }
    }

    @Override
    public void moveAlongTrack(ServerLevel level) {
        for (NewMinecartBehavior.TrackIteration trackIteration = new NewMinecartBehavior.TrackIteration();
             trackIteration.shouldIterate() && this.minecart.isAlive();
             trackIteration.firstIteration = false
        ) {
            Vec3 initialStepDeltaMovement = this.getDeltaMovement();
            BlockPos currentPos = this.minecart.getCurrentBlockPosOrRailBelow();
            BlockState currentState = this.level().getBlockState(currentPos);
            boolean onRails = BaseRailBlock.isRail(currentState);
            if (this.minecart.isOnRails() != onRails) {
                this.minecart.setOnRails(onRails);
                this.adjustToRails(currentPos, currentState, false);
            }

            if (onRails) {
                this.minecart.resetFallDistance();
                this.minecart.setOldPosAndRot();
                if (currentState.is(Blocks.ACTIVATOR_RAIL)) {
                    this.minecart.activateMinecart(level, currentPos.getX(), currentPos.getY(), currentPos.getZ(), currentState.getValue(PoweredRailBlock.POWERED));
                }

                RailShape shape = currentState.getValue(((BaseRailBlock)currentState.getBlock()).getShapeProperty());
                Vec3 newDeltaMovement = this.calculateTrackSpeed(level, initialStepDeltaMovement.horizontal(), trackIteration, currentPos, currentState, shape);
                if (trackIteration.firstIteration) {
                    trackIteration.movementLeft = newDeltaMovement.horizontalDistance();
                } else {
                    trackIteration.movementLeft = trackIteration.movementLeft + (newDeltaMovement.horizontalDistance() - initialStepDeltaMovement.horizontalDistance());
                }

                this.setDeltaMovement(newDeltaMovement);
                trackIteration.movementLeft = this.minecart.makeStepAlongTrack(currentPos, shape, trackIteration.movementLeft);
            } else {
                this.minecart.comeOffTrack(level);
                trackIteration.movementLeft = 0.0;
            }

            Vec3 stepPosition = this.position();
            Vec3 stepDelta = stepPosition.subtract(this.minecart.oldPosition());
            double stepLength = stepDelta.length();
            if (stepLength > 1.0E-5F) {
                if (!(stepDelta.horizontalDistanceSqr() > 1.0E-5F)) {
                    if (!this.minecart.isOnRails()) {
                        this.setXRot(this.minecart.onGround() ? 0.0F : Mth.rotLerp(0.2F, this.getXRot(), 0.0F));
                    }
                } else {
                    float yRot = 180.0F - (float)(Math.atan2(stepDelta.z, stepDelta.x) * 180.0 / Math.PI);
                    float xRot = this.minecart.onGround() && !this.minecart.isOnRails()
                            ? 0.0F
                            : 90.0F - (float)(Math.atan2(stepDelta.horizontalDistance(), stepDelta.y) * 180.0 / Math.PI);
                    yRot += this.minecart.isFlipped() ? 180.0F : 0.0F;
                    xRot *= this.minecart.isFlipped() ? -1.0F : 1.0F;
                    this.setRotation(yRot, xRot);
                }

                this.lerpSteps
                        .add(
                                new NewMinecartBehavior.MinecartStep(
                                        stepPosition, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float)Math.min(stepLength, this.getMaxSpeed(level))
                                )
                        );
            } else if (trackIteration.firstIteration && initialStepDeltaMovement.horizontalDistanceSqr() > 0.0) {
                this.lerpSteps.add(new MinecartStep(stepPosition, this.getDeltaMovement(), this.getYRot(), this.getXRot(), 1.0E-4F));
            }

            if (stepLength > 1.0E-5F || trackIteration.firstIteration) {
                this.minecart.applyEffectsFromBlocks();
                this.minecart.applyEffectsFromBlocks();
            }
        }
    }

    @Override
    public double stepAlongTrack(final BlockPos pos, final RailShape shape, double movementLeft) {
        if (movementLeft < 1.0E-5F) {
            return 0.0;
        }

        Vec3 oldPosition = this.position();
        BlockState railState = this.level().getBlockState(pos);
        Pair<Vec3i, Vec3i> exits = ((BaseRailBlock) railState.getBlock()).getRailExits(this.getDeltaMovement(), shape);
        Vec3i exit0 = exits.getFirst();
        Vec3i exit1 = exits.getSecond();
        Vec3 movement = this.getDeltaMovement().horizontal();
        if (movement.length() < 1.0E-5F) {
            this.setDeltaMovement(Vec3.ZERO);
            return 0.0;
        }

        boolean inHill = exit0.getY() != exit1.getY();
        Vec3 horizontalInDirection = new Vec3(exit1).scale(0.5).horizontal();
        Vec3 horizontalOutDirection = new Vec3(exit0).scale(0.5).horizontal();
        if (movement.dot(horizontalOutDirection) < movement.dot(horizontalInDirection)) {
            horizontalOutDirection = horizontalInDirection;
        }

        Vec3 outPosition = Vec3.atBottomCenterOf(pos).add(horizontalOutDirection).add(0.0, 0.1, 0.0).add(horizontalOutDirection.normalize().scale(1.0E-5F));
        if (inHill && !this.isDecending(movement, shape)) {
            outPosition = outPosition.add(0.0, 1.0, 0.0);
        }

        Vec3 towardsOut = outPosition.subtract(this.position()).normalize();
        movement = towardsOut.scale(movement.length() / towardsOut.horizontalDistance());
        Vec3 newPosition = oldPosition.add(movement.normalize().scale(movementLeft * (inHill ? Mth.SQRT_OF_TWO : 1.0F)));
        if (oldPosition.distanceToSqr(outPosition) <= oldPosition.distanceToSqr(newPosition)) {
            movementLeft = outPosition.subtract(newPosition).horizontalDistance();
            newPosition = outPosition;
        } else {
            movementLeft = 0.0;
        }

        this.minecart.move(MoverType.SELF, newPosition.subtract(oldPosition));
        BlockState newBlockState = this.level().getBlockState(BlockPos.containing(newPosition));
        if (inHill) {
            if (BaseRailBlock.isRail(newBlockState)) {
                RailShape newRailShape = newBlockState.getValue(((BaseRailBlock)newBlockState.getBlock()).getShapeProperty());
                if (this.restAtVShape(shape, newRailShape)) {
                    return 0.0;
                }
            }

            double horizontalDistanceFromOut = outPosition.horizontal().distanceTo(this.position().horizontal());
            double projectYPos = outPosition.y + (this.isDecending(movement, shape) ? horizontalDistanceFromOut : -horizontalDistanceFromOut);
            if (this.position().y < projectYPos) {
                this.setPos(this.position().x, projectYPos, this.position().z);
            }
        }

        if (this.position().distanceTo(oldPosition) < 1.0E-5F && newPosition.distanceTo(oldPosition) > 1.0E-5F) {
            this.setDeltaMovement(Vec3.ZERO);
            return 0.0;
        } else {
            this.setDeltaMovement(movement);
            return movementLeft;
        }
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

        if (trackIteration.firstIteration) {
            newDeltaMovement = this.calculateTrackDirection(newDeltaMovement, currentState);
            newDeltaMovement = this.calculateTrackDeltaMovementModifier(newDeltaMovement, currentState);
        }

        return newDeltaMovement;
    }

    private Vec3 calculateTrackDeltaMovementModifier(Vec3 deltaMovement, BlockState state) {
        if (state.getBlock() instanceof IRailBlock railBlock) {
            return railBlock.calculateDeltaMovement(deltaMovement, state);
        }

        return deltaMovement;
    }

    private Vec3 calculateTrackDirection(Vec3 deltaMovement, BlockState state) {
        if (state.getBlock() instanceof IRailBlock railBlock && railBlock.getRailDirection(state).getDirection() != null) {
            return railBlock.getRailDirection(state).getDirection().scale(deltaMovement.horizontalDistance()).add(0, deltaMovement.y(), 0);
        }

        return deltaMovement;
    }

    private Vec3 applyFriction(Vec3 deltaMovement, BlockState state) {
        float friction = 0.01f;

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
                return deltaMovement.normalize().scale(deltaMovement.length() + 0.8f / 20f);
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