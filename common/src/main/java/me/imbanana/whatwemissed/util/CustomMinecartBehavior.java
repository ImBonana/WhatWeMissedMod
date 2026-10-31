package me.imbanana.whatwemissed.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CustomMinecartBehavior extends NewMinecartBehavior {
    public CustomMinecartBehavior(AbstractMinecart minecart) {
        super(minecart);
    }

    @Override
    public void setRotation(float yRot, float xRot) {
        Vec3 movement = this.getDeltaMovement().horizontal();

        if (movement.lengthSqr() > 1) {
            yRot = MinecartMath.yRotFromMovement(movement);
        } else if (Math.abs(Mth.wrapDegrees(yRot - this.getYRot())) > 135.0F) {
            yRot = this.getYRot();
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

    // Modified
    @Override
    public double getMaxSpeed(final ServerLevel level) {
        return 40 * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
    }
}