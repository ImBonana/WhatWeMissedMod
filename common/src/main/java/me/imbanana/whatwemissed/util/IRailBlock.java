package me.imbanana.whatwemissed.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public interface IRailBlock {
    default float getRailFrictionModifier() {
        return 1;
    }

    default Pair<Vec3i, Vec3i> getRailExits(Vec3 deltaMovement, RailShape shape) {
        return AbstractMinecart.exits(shape);
    }

    default RailDirection getRailDirection(BlockState state) {
        return RailDirection.FREESTYLE;
    }

    default Vec3 calculateDeltaMovement(Vec3 deltaMovement, BlockState state) {
        return deltaMovement;
    }

    enum RailDirection {
        NORTH(Direction.Axis.Z.getPositive().getUnitVec3()),
        SOUTH(Direction.Axis.Z.getNegative().getUnitVec3()),
        EAST(Direction.Axis.X.getPositive().getUnitVec3()),
        WEST(Direction.Axis.X.getNegative().getUnitVec3()),
        FREESTYLE(null);

        private final Vec3 direction;

        RailDirection(Vec3 direction) {
            this.direction = direction;
        }

        public Vec3 getDirection() {
            return direction;
        }
    }
}
