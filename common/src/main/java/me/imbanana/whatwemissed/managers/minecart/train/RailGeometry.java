package me.imbanana.whatwemissed.managers.minecart.train;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class RailGeometry {
    public static final double ON_LINE_TOLERANCE = 0.02;

    private static final int[] NEIGHBOUR_HEIGHTS = {0, 1, -1};
    private static final int MAX_TRACK_BLOCKS = 16;

    public static BlockPos railUnder(Level level, Vec3 position) {
        BlockPos pos = BlockPos.containing(position);
        if (BaseRailBlock.isRail(level.getBlockState(pos))) return pos;

        BlockPos below = pos.below();
        return BaseRailBlock.isRail(level.getBlockState(below)) ? below : null;
    }

    public static boolean onRailLine(Level level, Vec3 position) {
        BlockPos rail = railUnder(level, position);
        return rail != null && distanceToRailLine(level, rail, position) <= ON_LINE_TOLERANCE;
    }

    public static boolean isFlatStraight(Level level, BlockPos pos) {
        List<Pair<Vec3i, Vec3i>> pairs = getExitPairs(level.getBlockState(pos));
        for (Pair<Vec3i, Vec3i> exits : pairs) {
            int dx = exits.getSecond().getX() - exits.getFirst().getX();
            int dz = exits.getSecond().getZ() - exits.getFirst().getZ();
            boolean straight = (dx == 0) != (dz == 0);
            boolean flat = exits.getFirst().getY() == exits.getSecond().getY();
            if (!straight || !flat) return false;
        }

        return !pairs.isEmpty();
    }

    public static double distanceToRailLine(Level level, BlockPos pos, Vec3 position) {
        double best = Double.MAX_VALUE;
        for (Pair<Vec3i, Vec3i> exits : getExitPairs(level.getBlockState(pos))) {
            boolean slope = exits.getFirst().getY() != exits.getSecond().getY();
            Vec3 start = exitPoint(pos, exits.getFirst(), slope);
            Vec3 end = exitPoint(pos, exits.getSecond(), slope);
            Vec3 line = end.subtract(start);
            double length = line.horizontalDistance();
            if (length < 1.0E-9) continue;

            double sideways = Math.abs((position.x - start.x) * line.z - (position.z - start.z) * line.x) / length;
            best = Math.min(best, sideways);
        }

        return best;
    }

    public static Vec3 directionAlongRail(Level level, BlockPos pos, Vec3 approximate) {
        Vec3 best = null;
        double bestAlignment = -Double.MAX_VALUE;
        for (Pair<Vec3i, Vec3i> exits : getExitPairs(level.getBlockState(pos))) {
            Vec3 direction = new Vec3(exits.getSecond().subtract(exits.getFirst())).horizontal();
            if (direction.lengthSqr() < 1.0E-9) continue;

            direction = direction.normalize();
            double alignment = direction.x * approximate.x + direction.z * approximate.z;
            if (alignment < 0) {
                direction = direction.scale(-1.0);
                alignment = -alignment;
            }
            if (alignment > bestAlignment) {
                best = direction;
                bestAlignment = alignment;
            }
        }

        return best;
    }

    public static List<Vec3> trackBeyond(Level level, Vec3 position, Vec3 away, double maxLength) {
        List<Vec3> points = new ArrayList<>();
        BlockPos pos = railUnder(level, position);
        if (pos == null) return points;

        Vec3i exit = null;
        double bestAlignment = -Double.MAX_VALUE;
        for (Pair<Vec3i, Vec3i> exits : getExitPairs(level.getBlockState(pos))) {
            for (Vec3i candidate : List.of(exits.getFirst(), exits.getSecond())) {
                double alignment = candidate.getX() * away.x + candidate.getZ() * away.z;
                if (alignment > bestAlignment) {
                    bestAlignment = alignment;
                    exit = candidate;
                }
            }
        }

        if (exit == null) return points;

        double length = 0;
        Vec3 previous = position;
        for (int block = 0; block < MAX_TRACK_BLOCKS; block++) {
            Vec3 edge = exitPoint(pos, exit, isSlopeExit(level.getBlockState(pos), exit));
            length += previous.distanceTo(edge);
            previous = edge;
            points.add(edge);
            if (length >= maxLength) break;

            BlockPos next = neighbourThrough(level, pos, exit);
            if (next == null) break;

            Vec3i onwards = null;
            for (Pair<Vec3i, Vec3i> exits : getExitPairs(level.getBlockState(next))) {
                if (pointsBack(exits.getFirst(), exit)) {
                    onwards = exits.getSecond();
                } else if (pointsBack(exits.getSecond(), exit)) {
                    onwards = exits.getFirst();
                }

                if (onwards != null) break;
            }

            if (onwards == null) break;

            pos = next;
            exit = onwards;
        }

        return points;
    }

    public static List<Vec3> railPath(Level level, Vec3 from, Vec3 to) {
        BlockPos start = railUnder(level, from);
        BlockPos goal = railUnder(level, to);
        if (start == null || goal == null) return null;
        if (start.equals(goal)) return List.of();

        int maxSteps = (int) Math.ceil(from.distanceTo(to)) + 3;

        Map<BlockPos, BlockPos> parent = new HashMap<>();
        Map<BlockPos, Integer> steps = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        steps.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty() && !parent.containsKey(goal)) {
            BlockPos pos = queue.poll();
            int distance = steps.get(pos);
            if (distance >= maxSteps) continue;

            for (Pair<Vec3i, Vec3i> exits : getExitPairs(level.getBlockState(pos))) {
                for (Vec3i exit : List.of(exits.getFirst(), exits.getSecond())) {
                    BlockPos next = neighbourThrough(level, pos, exit);
                    if (next != null && !steps.containsKey(next)) {
                        steps.put(next, distance + 1);
                        parent.put(next, pos);
                        queue.add(next);
                    }
                }
            }
        }

        if (!parent.containsKey(goal)) return null;

        List<Vec3> points = new ArrayList<>();
        for (BlockPos pos = goal; !pos.equals(start); pos = parent.get(pos)) {
            Vec3 edge = edgeTowards(level, parent.get(pos), pos);
            if (edge == null) return null;
            points.addFirst(edge);
        }

        return points;
    }

    private static Vec3 edgeTowards(Level level, BlockPos pos, BlockPos next) {
        for (Pair<Vec3i, Vec3i> exits : getExitPairs(level.getBlockState(pos))) {
            boolean slope = exits.getFirst().getY() != exits.getSecond().getY();
            for (Vec3i exit : List.of(exits.getFirst(), exits.getSecond())) {
                if (Objects.equals(neighbourThrough(level, pos, exit), next)) {
                    return exitPoint(pos, exit, slope);
                }
            }
        }

        return null;
    }

    private static BlockPos neighbourThrough(Level level, BlockPos pos, Vec3i exit) {
        for (int dy : NEIGHBOUR_HEIGHTS) {
            BlockPos candidate = pos.offset(exit.getX(), dy, exit.getZ());

            for (Pair<Vec3i, Vec3i> exits : getExitPairs(level.getBlockState(candidate))) {
                if (pointsBack(exits.getFirst(), exit) || pointsBack(exits.getSecond(), exit)) return candidate;
            }
        }

        return null;
    }

    private static boolean pointsBack(Vec3i candidate, Vec3i exit) {
        return candidate.getX() == -exit.getX() && candidate.getZ() == -exit.getZ();
    }

    private static boolean isSlopeExit(BlockState state, Vec3i exit) {
        for (Pair<Vec3i, Vec3i> exits : getExitPairs(state)) {
            if (exits.getFirst().equals(exit) || exits.getSecond().equals(exit)) {
                return exits.getFirst().getY() != exits.getSecond().getY();
            }
        }

        return false;
    }

    private static List<Pair<Vec3i, Vec3i>> getExitPairs(BlockState state) {
        if (!BaseRailBlock.isRail(state)) return List.of();

        BaseRailBlock rail = (BaseRailBlock) state.getBlock();
        return rail.getAllRailExits(state.getValue(rail.getShapeProperty()));
    }

    private static Vec3 exitPoint(BlockPos pos, Vec3i exit, boolean slope) {
        double height = NewMinecartBehavior.ON_RAIL_Y_OFFSET + (slope && exit.getY() >= 0 ? 1.0 : 0.0);
        return new Vec3(pos.getX() + 0.5 + exit.getX() * 0.5, pos.getY() + height, pos.getZ() + 0.5 + exit.getZ() * 0.5);
    }
}
