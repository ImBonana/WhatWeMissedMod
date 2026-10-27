package me.imbanana.whatwemissed.util;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

public final class MinecartMath {
    public static Vec3 forward(AbstractMinecart cart) {
        double r = Math.toRadians(cart.getYRot());
        return new Vec3(-Math.cos(r), 0.0, Math.sin(r));
    }

    public static float yRotFromMovement(Vec3 movement) {
        return 180.0F - (float) (Math.atan2(movement.z, movement.x) * 180.0 / Math.PI);
    }
}
