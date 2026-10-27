package me.imbanana.whatwemissed.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;

public class CustomMinecartBehavior extends NewMinecartBehavior {
    public CustomMinecartBehavior(AbstractMinecart minecart) {
        super(minecart);
    }

    @Override
    public double getMaxSpeed(ServerLevel level) {
        return 16;
    }
}
