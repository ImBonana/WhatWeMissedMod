package me.imbanana.whatwemissed.managers.minecart.train;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class TrainState {
    private static final double EXTENSION_LENGTH = 4.0;

    private final List<UUID> members;
    private final List<Vec3> spine = new ArrayList<>();
    private final double[] gaps;
    private boolean frontIsHead = true;
    private int airborneTicks;

    private TrainState(List<UUID> members, double[] gaps) {
        this.members = members;
        this.gaps = gaps;
    }

    public static TrainState create(TrainChain chain) {
        int trainSize = chain.size();
        TrainState state = new TrainState(chain.memberIds(), new double[trainSize - 1]);
        Level level = chain.cart(0).level();

        state.spine.add(chain.cart(0).position());
        for (int i = 1; i < trainSize; i++) {
            Vec3 from = chain.cart(i - 1).position();
            Vec3 to = chain.cart(i).position();
            List<Vec3> between = RailGeometry.railPath(level, from, to);

            double length = 0;
            Vec3 previous = from;
            if (between != null) {
                for (Vec3 point : between) {
                    length += previous.distanceTo(point);
                    previous = point;
                    state.spine.add(point);
                }
            }
            length += previous.distanceTo(to);
            state.spine.add(to);
            state.gaps[i - 1] = Math.max(length, 0.05);
        }

        Vec3 last = chain.cart(trainSize - 1).position();
        Vec3 away = last.subtract(chain.cart(trainSize - 2).position());
        state.spine.addAll(RailGeometry.trackBeyond(level, last, away, EXTENSION_LENGTH));
        return state;
    }

    public boolean matches(TrainChain chain) {
        if (this.members.size() != chain.size()) return false;
        for (int i = 0; i < this.members.size(); i++) {
            if (!this.members.get(i).equals(chain.cart(i).getUUID())) return false;
        }

        return true;
    }

    public boolean isFrontIsHead() {
        return this.frontIsHead;
    }

    public void setFrontIsHead(boolean frontIsHead) {
        if (this.frontIsHead == frontIsHead) return;

        double extension = Math.max(0.0, this.length() - this.getTotalGap());
        this.frontIsHead = frontIsHead;
        Collections.reverse(this.spine);
        this.dropFront(extension);
    }

    public double getGap(int index) {
        return this.gaps[this.getSlot(index)];
    }

    public void setGap(int index, double gap) {
        this.gaps[this.getSlot(index)] = gap;
    }

    public double[] copyGaps() {
        double[] copy = new double[this.gaps.length];
        for (int i = 0; i < copy.length; i++) copy[i] = this.getGap(i);
        return copy;
    }

    public void setGaps(double[] gaps) {
        for (int i = 0; i < gaps.length; i++) this.setGap(i, gaps[i]);
    }

    private int getSlot(int index) {
        return this.frontIsHead ? index : this.gaps.length - 1 - index;
    }

    public void easeGapsTowards(double target, double step) {
        for (int i = 0; i < this.gaps.length; i++) {
            this.gaps[i] += Math.clamp(target - this.gaps[i], -step, step);
        }
    }

    public double getTotalGap() {
        double total = 0;
        for (double gap : this.gaps) total += gap;
        return total;
    }

    public int getAirborneTicks() {
        return this.airborneTicks;
    }

    public void setAirborneTicks(int airborneTicks) {
        this.airborneTicks = airborneTicks;
    }

    public void pushFront(Vec3 point) {
        if (this.spine.getFirst().distanceToSqr(point) < 1.0E-8) return;
        this.spine.addFirst(point);
    }

    public void dropFront(double distance) {
        if (distance <= 1.0E-6 || this.spine.size() < 2) return;

        Vec3 newFront = this.pointAt(distance);
        double travelled = 0;
        for (int i = 1; i < this.spine.size(); i++) {
            travelled += this.spine.get(i).distanceTo(this.spine.get(i - 1));
            if (travelled > distance) {
                this.spine.subList(0, i).clear();
                this.spine.addFirst(newFront);
                return;
            }
        }

        Vec3 last = this.spine.getLast();
        this.spine.clear();
        this.spine.add(last);
    }

    public void trim(double length) {
        double travelled = 0;
        for (int i = 1; i < this.spine.size(); i++) {
            travelled += this.spine.get(i).distanceTo(this.spine.get(i - 1));
            if (travelled > length) {
                this.spine.subList(i + 1, this.spine.size()).clear();
                return;
            }
        }
    }

    public Vec3 pointAt(double distance) {
        if (this.spine.size() == 1 || distance <= 0) return this.spine.getFirst();

        double travelled = 0;
        for (int i = 1; i < this.spine.size(); i++) {
            Vec3 from = this.spine.get(i - 1);
            Vec3 to = this.spine.get(i);
            double segment = from.distanceTo(to);
            if (travelled + segment >= distance) {
                return segment < 1.0E-9 ? to : from.lerp(to, (distance - travelled) / segment);
            }
            travelled += segment;
        }

        return this.spine.getLast();
    }

    public List<Vec3> pathBetween(double nearer, double farther) {
        List<Vec3> points = new ArrayList<>();
        points.add(this.pointAt(nearer));

        double travelled = 0;
        for (int i = 1; i < this.spine.size() && travelled < farther; i++) {
            travelled += this.spine.get(i).distanceTo(this.spine.get(i - 1));
            if (travelled > nearer && travelled < farther) points.add(this.spine.get(i));
        }

        points.add(this.pointAt(farther));
        Collections.reverse(points);
        return points;
    }

    private double length() {
        double total = 0;
        for (int i = 1; i < this.spine.size(); i++) {
            total += this.spine.get(i).distanceTo(this.spine.get(i - 1));
        }
        return total;
    }
}
