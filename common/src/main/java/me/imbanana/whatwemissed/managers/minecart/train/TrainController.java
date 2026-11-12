package me.imbanana.whatwemissed.managers.minecart.train;

import com.mojang.datafixers.util.Pair;
import me.imbanana.whatwemissed.util.ChainedCartLinkSide;
import me.imbanana.whatwemissed.util.CustomMinecartBehavior;
import me.imbanana.whatwemissed.util.IRailBlock;
import me.imbanana.whatwemissed.util.MinecartMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TrainController {
    private static final double SPACING = 2.2;
    private static final double GAP_EASE = 0.1;
    private static final int LANDING_AIRBORNE_TICKS = 3;
    private static final double PATH_MARGIN = 4.0;
    private static final double PATH_DIRECTION_SPAN = 0.4;
    private static final double MIN_ORIENTATION_LENGTH = 0.3;
    private static final double STATIONARY_ADVANCE = 0.01;
    private static final double MIN_RAIL_ALIGNMENT = 0.5;
    private static final double SWEEP_STEP = 0.5;
    private static final int SIDEWAYS_SUBSTEPS = 5;
    private static final double COLLISION_SKIN = 0.01;

    public static void moveAlongTrack(ServerLevel level, AbstractMinecart caller) {
        long now = level.getGameTime();
        if (caller.what_we_missed$getTrainTick() == now) return;

        TrainChain chain = TrainChain.of(caller);
        int trainSize = chain.size();
        if (trainSize < 2) {
            behaviorOf(caller).followTrack(level);
            return;
        }

        TrainState state = caller.what_we_missed$getTrainState();
        if (state == null || !state.matches(chain)) state = TrainState.create(chain);
        for (AbstractMinecart cart : chain.carts()) {
            cart.what_we_missed$setTrainState(state);
            cart.what_we_missed$setTrainTick(now);
        }

        Vec3[] towardsHead = directionsTowardsHead(chain, state);
        double speed = nextSpeed(level, chain, towardsHead, currentSpeed(chain, towardsHead));

        boolean frontIsHead = speed > 0 || (speed == 0 && state.isFrontIsHead());
        state.setFrontIsHead(frontIsHead);
        List<AbstractMinecart> carts = frontIsHead ? chain.carts() : chain.carts().reversed();
        AbstractMinecart front = carts.getFirst();
        CustomMinecartBehavior frontBehavior = behaviorOf(front);
        boolean frontFacesFront = isFacingFront(chain, front, frontIsHead);

        Vec3 outward = frontIsHead ? towardsHead[0] : towardsHead[trainSize - 1].scale(-1.0);
        double magnitude = Math.abs(speed);

        BlockPos frontRail = RailGeometry.railUnder(level, front.position());
        Vec3 alongRail = frontRail == null ? null : RailGeometry.directionAlongRail(level, frontRail, outward);
        if (alongRail != null) {
            if (alongRail.x * outward.x + alongRail.z * outward.z < MIN_RAIL_ALIGNMENT) magnitude = 0;
            outward = alongRail;
        }
        front.setDeltaMovement(outward.x * magnitude, front.getDeltaMovement().y, outward.z * magnitude);

        Vec3 frontOld = front.position();
        if (state.pointAt(0).distanceToSqr(frontOld) > 0.0025) state.pushFront(frontOld);

        List<Vec3> path = new ArrayList<>();
        int stepsBefore = frontBehavior.lerpSteps.size();
        frontBehavior.followTrackAsFront(level, path, !frontFacesFront);

        double advanced = 0;
        Vec3 previous = frontOld;
        for (Vec3 point : path) {
            advanced += previous.distanceTo(point);
            previous = point;
            state.pushFront(point);
        }

        double finalSpeed = front.getDeltaMovement().horizontalDistance();
        double[] gapsBefore = state.copyGaps();
        state.easeGapsTowards(SPACING, GAP_EASE);

        int firstCross = firstCrossTrackLink(level, carts);
        for (int i = firstCross; i < trainSize; i++) {
            state.setGap(i - 1, gapsBefore[i - 1]);
        }

        double rewind = findFreeRewind(level, carts, state, gapsBefore, advanced, firstCross);
        if (rewind > 0) {
            state.setGaps(gapsBefore);
            rewind = Math.min(findFreeRewind(level, carts, state, gapsBefore, advanced, firstCross), advanced);
        }

        Vec3[] coupled = null;
        if (firstCross < trainSize) {
            CoupledResult result = placeCoupled(level, carts, state, firstCross, rewind, advanced);
            rewind = result.rewind();
            coupled = result.positions();
        }

        if (rewind > 0) {
            state.dropFront(rewind);
            advanced = Math.max(0.0, advanced - rewind);
            finalSpeed = 0;

            front.setPos(state.pointAt(0));
            front.setDeltaMovement(0.0, front.getDeltaMovement().y, 0.0);
            settleCartInBlock(level, front);

            while (frontBehavior.lerpSteps.size() > stepsBefore) frontBehavior.lerpSteps.removeLast();
            frontBehavior.lerpSteps.add(new NewMinecartBehavior.MinecartStep(front.position(), Vec3.ZERO, front.getYRot(), front.getXRot(), (float) Math.max(advanced, 1.0E-4)));
        }

        Vec3[] targets = new Vec3[trainSize];
        double[] distances = new double[trainSize];
        targets[0] = front.position();
        double distance = 0;
        for (int i = 1; i < trainSize; i++) {
            distance += state.getGap(i - 1);
            distances[i] = distance;
            if (i < firstCross) {
                targets[i] = state.pointAt(distance);
            } else {
                targets[i] = coupled != null ? coupled[i] : carts.get(i).position();
            }
        }

        Vec3[] towardsFront = new Vec3[trainSize];
        for (int j = 0; j < trainSize; j++) {
            towardsFront[j] = targets[Math.max(j - 1, 0)].subtract(targets[Math.min(j + 1, trainSize - 1)]);
        }

        for (int j = 1; j < trainSize; j++) {
            AbstractMinecart cart = carts.get(j);
            boolean onPath = j < firstCross;
            Vec3 movement = onPath ? movementAlong(towardsFront[j], finalSpeed) : targets[j].subtract(cart.position());
            placeFollower(level, cart, state, targets[j], distances[j], onPath ? advanced : 0.0, movement, towardsFront[j], isFacingFront(chain, cart, frontIsHead));
        }

        if (advanced < STATIONARY_ADVANCE) orientStationaryFront(level, front, towardsFront[0], frontFacesFront);

        state.trim(state.getTotalGap() + PATH_MARGIN);
        updateCartInAir(level, state, front, frontIsHead);
    }

    private static Vec3[] directionsTowardsHead(TrainChain chain, TrainState state) {
        int trainSize = chain.size();
        boolean frontIsHead = state.isFrontIsHead();
        Vec3[] towardsHead = new Vec3[trainSize];
        double distance = 0;
        for (int i = 0; i < trainSize; i++) {
            if (i > 0) distance += state.getGap(i - 1);
            int index = frontIsHead ? i : trainSize - 1 - i;

            Vec3 toFront = pathDirection(state, distance);
            if (toFront != null) {
                towardsHead[index] = frontIsHead ? toFront : toFront.scale(-1.0);
                continue;
            }

            Vec3 ahead = chain.cart(Math.max(index - 1, 0)).position();
            Vec3 behind = chain.cart(Math.min(index + 1, trainSize - 1)).position();
            Vec3 direction = new Vec3(ahead.x - behind.x, 0.0, ahead.z - behind.z);
            towardsHead[index] = direction.lengthSqr() < 1.0E-8 ? new Vec3(1.0, 0.0, 0.0) : direction.normalize();
        }

        return towardsHead;
    }

    private static Vec3 pathDirection(TrainState state, double distance) {
        Vec3 ahead = state.pointAt(Math.max(0.0, distance - PATH_DIRECTION_SPAN));
        Vec3 behind = state.pointAt(distance + PATH_DIRECTION_SPAN);
        Vec3 direction = new Vec3(ahead.x - behind.x, 0.0, ahead.z - behind.z);
        return direction.lengthSqr() < 0.05 * 0.05 ? null : direction.normalize();
    }

    private static double currentSpeed(TrainChain chain, Vec3[] towardsHead) {
        double sum = 0;
        for (int i = 0; i < chain.size(); i++) {
            Vec3 movement = chain.cart(i).getDeltaMovement();
            sum += movement.x * towardsHead[i].x + movement.z * towardsHead[i].z;
        }

        double speed = sum / chain.size();
        return Math.abs(speed) < 1.0E-5 ? 0.0 : speed;
    }

    private static double nextSpeed(ServerLevel level, TrainChain chain, Vec3[] towardsHead, double speed) {
        int n = chain.size();
        double force = 0;
        double maxSpeed = Double.MAX_VALUE;
        double fixedSpeed = -1;
        double fixedSign = 1;

        for (int i = 0; i < n; i++) {
            AbstractMinecart cart = chain.cart(i);
            maxSpeed = Math.min(maxSpeed, cart.getMaxSpeed(level));

            BlockPos pos = cart.getCurrentBlockPosOrRailBelow();
            BlockState railState = level.getBlockState(pos);
            if (!BaseRailBlock.isRail(railState)) continue;

            BaseRailBlock rail = (BaseRailBlock) railState.getBlock();
            RailShape shape = railState.getValue(rail.getShapeProperty());

            Vec3 movement = towardsHead[i].scale(speed);
            Vec3 change = behaviorOf(cart).evaluateForces(level, movement, pos, railState, shape).subtract(movement);
            force += change.x * towardsHead[i].x + change.z * towardsHead[i].z;

            double fixed = rail.getFixedSpeed(railState);
            if (fixed > 0 && (fixedSpeed < 0 || fixed < fixedSpeed)) {
                fixedSpeed = fixed;
                Vec3 facing = rail.getRailDirection(railState).getDirection();
                if (facing == null) fixedSign = speed >= 0 ? 1 : -1;
                else fixedSign = facing.x * towardsHead[i].x + facing.z * towardsHead[i].z >= 0 ? 1 : -1;
            }
        }

        double next;
        if (fixedSpeed > 0) {
            next = fixedSign * fixedSpeed;
        } else {
            next = speed + force / n;
            if (speed != 0 && Math.signum(next) != Math.signum(speed)) next = 0;
        }

        next = Math.clamp(next, -maxSpeed, maxSpeed);
        return Math.abs(next) < 1.0E-5 ? 0.0 : next;
    }

    private static int firstCrossTrackLink(ServerLevel level, List<AbstractMinecart> carts) {
        for (int i = 1; i < carts.size(); i++) {
            Vec3 ahead = carts.get(i - 1).position();
            Vec3 cart = carts.get(i).position();
            if (RailGeometry.railUnder(level, ahead) == null || RailGeometry.railUnder(level, cart) == null) continue;
            if (RailGeometry.railPath(level, ahead, cart) == null) return i;
        }

        return carts.size();
    }

    private static CoupledResult placeCoupled(ServerLevel level, List<AbstractMinecart> carts, TrainState state, int firstCross, double rewind, double advanced) {
        Vec3[] placed = coupledPositions(level, carts, state, firstCross, rewind);
        if (placed != null) return new CoupledResult(rewind, placed);

        Vec3[] standing = coupledPositions(level, carts, state, firstCross, advanced);
        if (standing == null) return new CoupledResult(advanced, null);

        double blocked = rewind;
        double free = advanced;
        placed = standing;
        for (int i = 0; i < 8; i++) {
            double middle = (blocked + free) / 2.0;
            Vec3[] attempt = coupledPositions(level, carts, state, firstCross, middle);
            if (attempt != null) {
                free = middle;
                placed = attempt;
            } else {
                blocked = middle;
            }
        }

        return new CoupledResult(free, placed);
    }

    private static Vec3[] coupledPositions(ServerLevel level, List<AbstractMinecart> carts, TrainState state, int firstCross, double rewind) {
        double distance = 0;
        for (int i = 1; i < firstCross; i++) {
            distance += state.getGap(i - 1);
        }

        Vec3 anchor = state.pointAt(distance + rewind);
        Vec3[] positions = new Vec3[carts.size()];
        for (int i = firstCross; i < carts.size(); i++) {
            Vec3 position = coupledPosition(level, carts.get(i), anchor, state.getGap(i - 1));
            if (position == null) return null;

            positions[i] = position;
            anchor = position;
        }

        return positions;
    }

    private static Vec3 coupledPosition(ServerLevel level, AbstractMinecart cart, Vec3 anchor, double length) {
        Vec3 current = cart.position();
        Vec3 offset = current.subtract(anchor);
        Vec3 towardsAnchor = new Vec3(-offset.x, 0.0, -offset.z);

        BlockPos rail = RailGeometry.railUnder(level, current);
        Vec3 along = rail != null ? RailGeometry.directionAlongRail(level, rail, towardsAnchor) : null;
        if (along == null) along = towardsAnchor.lengthSqr() < 1.0E-8 ? null : towardsAnchor.normalize();
        if (along == null) return Math.abs(offset.length() - length) < 1.0E-3 ? current : null;

        double offsetAlong = along.x * offset.x + along.z * offset.z;
        double discriminant = offsetAlong * offsetAlong - (offset.lengthSqr() - length * length);
        if (discriminant < 0) return null;

        double root = Math.sqrt(discriminant);
        double firstSlide = -offsetAlong + root;
        double secondSlide = -offsetAlong - root;
        double slide = Math.abs(firstSlide) < Math.abs(secondSlide) ? firstSlide : secondSlide;
        if (Math.abs(slide) < 1.0E-6) return current;

        double sidewaysLimit = sidewaysLimit(level, rail, current);
        double moved = 0;
        Vec3 previous = current;
        while (moved < Math.abs(slide)) {
            double step = Math.min(SWEEP_STEP, Math.abs(slide) - moved);
            Vec3 next = previous.add(along.scale(Math.signum(slide) * step));
            if (!canMove(level, cart, rail, sidewaysLimit, previous, next)) return null;

            previous = next;
            moved += step;
        }

        return current.add(along.scale(slide));
    }

    private static double findFreeRewind(ServerLevel level, List<AbstractMinecart> carts, TrainState state, double[] gapsBefore, double advanced, int firstCross) {
        double rewind = 0.0;
        double distance = 0.0;
        double distanceBefore = 0.0;
        for (int i = 1; i < firstCross; i++) {
            distance += state.getGap(i - 1);
            distanceBefore += gapsBefore[i - 1];
            double from = distanceBefore + advanced;
            if (Math.abs(from - distance) < 1.0E-4) continue;

            AbstractMinecart cart = carts.get(i);
            BlockPos rail = RailGeometry.railUnder(level, cart.position());
            double sidewaysLimit = sidewaysLimit(level, rail, cart.position());

            if (from < distance) {
                for (double at = from; at < distance; ) {
                    double next = Math.min(at + SWEEP_STEP, distance);
                    Vec3 start = at == from ? cart.position() : state.pointAt(at);
                    if (!canMove(level, cart, rail, sidewaysLimit, start, state.pointAt(next))) return Double.POSITIVE_INFINITY;
                    at = next;
                }
                continue;
            }

            for (double at = from; at > distance; ) {
                double next = Math.max(at - SWEEP_STEP, distance);
                Vec3 start = at == from ? cart.position() : state.pointAt(at);
                if (!canMove(level, cart, rail, sidewaysLimit, start, state.pointAt(next))) {
                    rewind = Math.max(rewind, narrowDown(level, cart, rail, sidewaysLimit, state, start, at, next) - distance);
                    break;
                }
                at = next;
            }
        }

        return rewind;
    }

    private static double narrowDown(ServerLevel level, AbstractMinecart cart, BlockPos currentRail, double sidewaysLimit, TrainState state, Vec3 start, double free, double blocked) {
        for (int i = 0; i < 5; i++) {
            double middle = (blocked + free) / 2.0;
            if (canMove(level, cart, currentRail, sidewaysLimit, start, state.pointAt(middle))) free = middle;
            else blocked = middle;
        }

        return free;
    }

    private static double sidewaysLimit(ServerLevel level, BlockPos rail, Vec3 position) {
        if (rail == null) return 0.0;
        return Math.max(RailGeometry.ON_LINE_TOLERANCE, RailGeometry.distanceToRailLine(level, rail, position) + 1.0E-6);
    }

    private static boolean canMove(ServerLevel level, AbstractMinecart cart, BlockPos currentRail, double sidewaysLimit, Vec3 from, Vec3 to) {
        if (currentRail != null) {
            for (int i = 1; i <= SIDEWAYS_SUBSTEPS; i++) {
                Vec3 point = from.lerp(to, (double) i / SIDEWAYS_SUBSTEPS);
                if (RailGeometry.distanceToRailLine(level, currentRail, point) > sidewaysLimit && !RailGeometry.onRailLine(level, point)) {
                    return false;
                }
            }
        }

        BlockPos rail = RailGeometry.railUnder(level, to);
        boolean onLine = rail != null && RailGeometry.distanceToRailLine(level, rail, to) <= RailGeometry.ON_LINE_TOLERANCE;
        if (onLine && !RailGeometry.isFlatStraight(level, rail)) return true;

        Vec3 movement = to.subtract(from);
        AABB box = cart.getBoundingBox().move(from.subtract(cart.position())).deflate(COLLISION_SKIN, 0.0, COLLISION_SKIN);
        if (onLine) {
            AABB withFront = restoreFrontFace(box, movement);
            if (level.noBlockCollision(cart, withFront)) box = withFront;
        }

        Vec3 allowed = Entity.collideBoundingBox(cart, movement, box, level, List.of());
        return allowed.distanceToSqr(movement) < 1.0E-12;
    }

    private static AABB restoreFrontFace(AABB box, Vec3 movement) {
        if (Math.abs(movement.x) >= Math.abs(movement.z)) {
            if (movement.x > 1.0E-9) return new AABB(box.minX, box.minY, box.minZ, box.maxX + COLLISION_SKIN, box.maxY, box.maxZ);
            if (movement.x < -1.0E-9) return new AABB(box.minX - COLLISION_SKIN, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        } else {
            if (movement.z > 1.0E-9) return new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ + COLLISION_SKIN);
            if (movement.z < -1.0E-9) return new AABB(box.minX, box.minY, box.minZ - COLLISION_SKIN, box.maxX, box.maxY, box.maxZ);
        }

        return box;
    }

    private static Vec3 movementAlong(Vec3 towardsFront, double speed) {
        double horizontal = towardsFront.horizontalDistance();
        if (horizontal > MIN_ORIENTATION_LENGTH && speed > 0) {
            Vec3 movement = towardsFront.scale(speed / horizontal);
            return Math.abs(movement.y) > speed ? new Vec3(movement.x, Math.signum(movement.y) * speed, movement.z) : movement;
        }

        return Vec3.ZERO;
    }

    private static void placeFollower(ServerLevel level, AbstractMinecart cart, TrainState state, Vec3 position, double distance, double advanced, Vec3 movement, Vec3 towardsFront, boolean facesFront) {
        Orientation orientation = orientationAt(level, position, cart.getYRot(), cart.getXRot(), towardsFront, facesFront);
        float yRot = orientation.yRot();
        float xRot = orientation.xRot();

        boolean moved = cart.position().distanceToSqr(position) > 1.0E-6
                || Math.abs(Mth.wrapDegrees(yRot - cart.getYRot())) > 0.5F
                || Math.abs(xRot - cart.getXRot()) > 0.5F;

        List<NewMinecartBehavior.MinecartStep> lerpSteps = behaviorOf(cart).lerpSteps;
        boolean added = false;
        if (advanced > 1.0E-6) {
            List<Vec3> points = state.pathBetween(distance, distance + advanced);
            for (int i = 1; i < points.size(); i++) {
                Vec3 segment = points.get(i).subtract(points.get(i - 1));
                double length = segment.length();
                if (length < 1.0E-6) continue;

                float stepYRot = yRot;
                float stepXRot = xRot;
                boolean last = i == points.size() - 1;
                if (!last && segment.horizontalDistance() >= MIN_ORIENTATION_LENGTH) {
                    stepYRot = yawFor(yRot, segment, facesFront);
                    stepXRot = pitchFor(xRot, segment, facesFront);
                }

                lerpSteps.add(new NewMinecartBehavior.MinecartStep(points.get(i), movement, stepYRot, stepXRot, (float) length));
                added = true;
            }
        }

        if (!added && moved) {
            float weight = (float) Math.max(cart.position().distanceTo(position), 1.0E-4);
            lerpSteps.add(new NewMinecartBehavior.MinecartStep(position, movement, yRot, xRot, weight));
        }

        cart.setOldPosAndRot();
        cart.setPos(position);
        cart.setYRot(yRot);
        cart.setXRot(xRot);
        cart.setDeltaMovement(movement);
        settleCartInBlock(level, cart);
    }

    private static void orientStationaryFront(ServerLevel level, AbstractMinecart front, Vec3 towardsFront, boolean facesFront) {
        Orientation orientation = orientationAt(level, front.position(), front.getYRot(), front.getXRot(), towardsFront, facesFront);
        float yRot = orientation.yRot();
        float xRot = orientation.xRot();
        if (Math.abs(Mth.wrapDegrees(yRot - front.getYRot())) < 0.5F && Math.abs(xRot - front.getXRot()) < 0.5F) return;

        front.setYRot(yRot);
        front.setXRot(xRot);
        behaviorOf(front).lerpSteps.add(new NewMinecartBehavior.MinecartStep(front.position(), Vec3.ZERO, yRot, xRot, 1.0E-4F));
    }

    private static Orientation orientationAt(ServerLevel level, Vec3 position, float currentYRot, float currentXRot, Vec3 towardsFront, boolean facesFront) {
        Orientation byNeighbours = new Orientation(yawFor(currentYRot, towardsFront, facesFront), pitchFor(currentXRot, towardsFront, facesFront));

        Vec3 forward = facesFront ? towardsFront : towardsFront.scale(-1.0);
        if (forward.horizontalDistance() < MIN_ORIENTATION_LENGTH) return byNeighbours;

        BlockPos pos = BlockPos.containing(position);
        if (BaseRailBlock.isRail(level.getBlockState(pos.below()))) pos = pos.below();
        BlockState state = level.getBlockState(pos);
        if (!BaseRailBlock.isRail(state)) return byNeighbours;

        RailShape shape = state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty());
        Pair<Vec3i, Vec3i> exits = AbstractMinecart.exits(shape);
        int dx = exits.getSecond().getX() - exits.getFirst().getX();
        int dz = exits.getSecond().getZ() - exits.getFirst().getZ();
        if (dx != 0 && dz != 0) return byNeighbours;

        Vec3 axis = new Vec3(Integer.signum(dx), 0.0, Integer.signum(dz));
        double alignment = (forward.x * axis.x + forward.z * axis.z) / forward.horizontalDistance();
        Vec3 direction;
        if (Math.abs(alignment) < MIN_RAIL_ALIGNMENT) {
            double radians = Math.toRadians(currentYRot);
            double current = -Math.cos(radians) * axis.x + Math.sin(radians) * axis.z;
            direction = current >= 0 ? axis : axis.scale(-1.0);
        } else {
            direction = alignment >= 0 ? axis : axis.scale(-1.0);
        }

        float pitch = 0.0F;
        int rise = exits.getSecond().getY() - exits.getFirst().getY();
        if (rise != 0) {
            Vec3 uphill = rise > 0 ? axis : axis.scale(-1.0);
            pitch = direction.dot(uphill) > 0 ? 45.0F : -45.0F;
        }

        return new Orientation(MinecartMath.yRotFromMovement(direction), pitch);
    }

    private static float yawFor(float current, Vec3 towardsFront, boolean facesFront) {
        if (towardsFront.horizontalDistance() < MIN_ORIENTATION_LENGTH) return current;
        return MinecartMath.yRotFromMovement(towardsFront) + (facesFront ? 0.0F : 180.0F);
    }

    private static float pitchFor(float current, Vec3 towardsFront, boolean facesFront) {
        double horizontal = towardsFront.horizontalDistance();
        if (horizontal < MIN_ORIENTATION_LENGTH) return current;

        float pitch = (float) Math.toDegrees(Math.atan2(towardsFront.y, horizontal));
        return Math.clamp(facesFront ? pitch : -pitch, -45.0F, 45.0F);
    }

    private static void settleCartInBlock(ServerLevel level, AbstractMinecart cart) {
        BlockPos pos = cart.getCurrentBlockPosOrRailBelow();
        BlockState railState = level.getBlockState(pos);
        boolean onRails = BaseRailBlock.isRail(railState);
        cart.setOnRails(onRails);
        if (onRails) {
            cart.resetFallDistance();
            if (railState.getBlock() instanceof IRailBlock railBlock && railBlock.isActivatorRail(railState)) {
                cart.activateMinecart(level, pos.getX(), pos.getY(), pos.getZ(), railBlock.isActivated(railState));
            }
        }

        cart.applyEffectsFromBlocks();
    }

    private static void updateCartInAir(ServerLevel level, TrainState state, AbstractMinecart front, boolean frontIsHead) {
        if (BaseRailBlock.isRail(level.getBlockState(front.getCurrentBlockPosOrRailBelow()))) {
            state.setAirborneTicks(0);
            return;
        }

        if (!front.onGround()) {
            state.setAirborneTicks(state.getAirborneTicks() + 1);
            return;
        }

        boolean landed = state.getAirborneTicks() >= LANDING_AIRBORNE_TICKS;
        state.setAirborneTicks(0);
        if (!landed) return;

        if (frontIsHead) {
            front.what_we_missed$unlinkChild();
        } else {
            front.what_we_missed$unlinkParent();
        }
    }

    private static boolean isFacingFront(TrainChain chain, AbstractMinecart cart, boolean frontIsHead) {
        boolean frontTowardsHead = cart == chain.cart(0)
                ? cart.what_we_missed$getChildSide() == ChainedCartLinkSide.BACK
                : cart.what_we_missed$getParentSide() == ChainedCartLinkSide.FRONT;
        return frontIsHead == frontTowardsHead;
    }

    private static CustomMinecartBehavior behaviorOf(AbstractMinecart cart) {
        return (CustomMinecartBehavior) cart.getBehavior();
    }

    private record Orientation(float yRot, float xRot) {
    }

    private record CoupledResult(double rewind, Vec3[] positions) {
    }
}
