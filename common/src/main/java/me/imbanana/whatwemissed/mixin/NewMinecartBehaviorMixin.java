package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.util.MinecartMath;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {
    protected NewMinecartBehaviorMixin(AbstractMinecart minecart) {
        super(minecart);
    }

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    private void what_we_missed$faceMovement(float yRot, float xRot, CallbackInfo ci) {
        Vec3 movement = this.getDeltaMovement().horizontal();

        if (movement.lengthSqr() > 1) {
            yRot = MinecartMath.yRotFromMovement(movement);
        } else if (Math.abs(Mth.wrapDegrees(yRot - this.getYRot())) > 135.0F) {
            yRot = this.getYRot();
        }

        xRot = Math.clamp(xRot, -45.0F, 45.0F);
        this.setXRot(xRot % 360.0F);
        this.setYRot(yRot % 360.0F);
        ci.cancel();
    }
}
