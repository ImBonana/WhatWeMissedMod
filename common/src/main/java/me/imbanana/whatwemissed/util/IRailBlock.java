package me.imbanana.whatwemissed.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public interface IRailBlock {
    default float getRailFrictionModifier() {
        return 1;
    }

    default Pair<Vec3i, Vec3i> getRailExits(Vec3 deltaMovement, RailShape shape) {
        return AbstractMinecart.exits(shape);
    }
}
