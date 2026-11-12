package me.imbanana.whatwemissed.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public interface IRailBlock {
    default float getRailFrictionModifier() {
        return 1;
    }

    default Pair<Vec3i, Vec3i> getRailExits(Vec3 deltaMovement, RailShape shape) {
        return AbstractMinecart.exits(shape);
    }

    default List<Pair<Vec3i, Vec3i>> getAllRailExits(RailShape shape) {
        return Constants.EXITS.get(shape);
    }

    default RailDirection getRailDirection(BlockState state) {
        return RailDirection.FREESTYLE;
    }

    default Vec3 calculateDeltaMovement(Vec3 deltaMovement, BlockState state) {
        return deltaMovement;
    }

    default double getFixedSpeed(BlockState state) {
        return -1;
    }

    default boolean isActivatorRail(BlockState state) {
        return state.is(Blocks.ACTIVATOR_RAIL);
    }

    default boolean isActivated(BlockState state) {
        return state.getValueOrElse(PoweredRailBlock.POWERED, false);
    }

    enum RailDirection {
        NORTH(Direction.Axis.Z.getNegative().getUnitVec3()),
        SOUTH(Direction.Axis.Z.getPositive().getUnitVec3()),
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

    final class Constants {
        private static final Map<RailShape, List<Pair<Vec3i, Vec3i>>> EXITS = new EnumMap<>(RailShape.class);

        static {
            for (RailShape shape : RailShape.values()) EXITS.put(shape, List.of(AbstractMinecart.exits(shape)));
        }
    }
}
