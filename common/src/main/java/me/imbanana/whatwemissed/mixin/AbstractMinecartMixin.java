package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.imbanana.whatwemissed.dataattachment.ModDataAttachmentTypes;
import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import me.imbanana.whatwemissed.util.ChainType;
import me.imbanana.whatwemissed.util.CustomMinecartBehavior;
import me.imbanana.whatwemissed.util.IAbstractMinecart;
import me.imbanana.whatwemissed.util.MinecartMath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity implements IAbstractMinecart {
    @Mutable
    @Shadow
    @Final
    private MinecartBehavior behavior;

    @Shadow
    public abstract void tick();

    @Unique
    private static final double REST = 2.2;
    @Unique
    private static final int ITERATIONS = 8;
    @Unique
    private static final double POS_SLOP = 0.002;
    @Unique
    private static final double MIN_DENOM = 0.25;

    @Unique
    private long what_we_missed$lastSolveTick = -1;

    public AbstractMinecartMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void changeBehavior(EntityType type, Level level, CallbackInfo ci) {
        this.behavior = new CustomMinecartBehavior((AbstractMinecart) (Object) this);
    }

    @Override
    public void what_we_missed$link(AbstractMinecart minecart, ChainType chainType) {
        ModDataAttachmentTypes.MINECART_LINK.set(this, new MinecartLinkData(Optional.of(minecart.getUUID()), chainType));
    }

    @Override
    public void what_we_missed$unlink() {
        if (this.level() instanceof ServerLevel level) {
            this.spawnAtLocation(level, new ItemStack(what_we_missed$getChainType().getDropItem(), 1));
        }

        ModDataAttachmentTypes.MINECART_LINK.remove(this);
    }

    @Override
    public boolean what_we_missed$hasLink() {
        return this.what_we_missed$getLinkedEntity() != null;
    }

    @Override
    public AbstractMinecart what_we_missed$getLinkedEntity() {
        MinecartLinkData minecartLinkData = ModDataAttachmentTypes.MINECART_LINK.get(this);
        if (minecartLinkData == null) return null;
        if (minecartLinkData.uuid().isEmpty()) return null;

        Entity entity = level().getEntity(minecartLinkData.uuid().get());
        if (!(entity instanceof AbstractMinecart minecart)) return null;
        if (!minecart.isAlive()) return null;

        return minecart;
    }

    @Override
    public ChainType what_we_missed$getChainType() {
        return ModDataAttachmentTypes.MINECART_LINK.getOrCreate(this).chainType();
    }

    @Override
    public boolean what_we_missed$isLinkedTo(AbstractMinecart minecart) {
        return minecart.equals(this.what_we_missed$getLinkedEntity());
    }

    @Override
    public long what_we_missed$getLastSolveTick() {
        return what_we_missed$lastSolveTick;
    }

    @Override
    public void what_we_missed$setLastSolveTick(long tick) {
        what_we_missed$lastSolveTick = tick;
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void solveTrain(CallbackInfo ci) {
        if (this.level().isClientSide()) return;

        long now = this.level().getGameTime();
        if (what_we_missed$lastSolveTick == now) return;

        List<AbstractMinecart> train = new ArrayList<>();
        Set<AbstractMinecart> seen = new HashSet<>();
        AbstractMinecart current = (AbstractMinecart) (Object) this;
        while (current != null && seen.add(current)) {
            train.add(current);
            current = current.what_we_missed$getLinkedEntity();
        }
        if (train.size() < 2) return;

        for (AbstractMinecart cart : train) {
            cart.what_we_missed$setLastSolveTick(now);
        }

        double trainSpeed = 0.0;
        for (AbstractMinecart cart : train) {
            trainSpeed = Math.max(trainSpeed, cart.getDeltaMovement().horizontalDistance());
        }
        double speedCap = Math.max(trainSpeed, 0.05) * 1.02;

        int iterations = Math.max(ITERATIONS, train.size());
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int i = 0; i < train.size() - 1; i++) {
                solveLink(train.get(i), train.get(i + 1), speedCap);
            }
            for (int i = train.size() - 2; i >= 0; i--) {
                solveLink(train.get(i), train.get(i + 1), speedCap);
            }
        }
    }

    @Unique
    private static void solveLink(AbstractMinecart a, AbstractMinecart b, double speedCap) {
        if (!a.isOnRails() || !b.isOnRails()) {
            solveFreeLink(a, b, speedCap);
            return;
        }
        if (Math.abs(b.getY() - a.getY()) > 3.0) return;

        double dx = b.getX() - a.getX();
        double dz = b.getZ() - a.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0E-4) return;

        double nx = dx / dist, nz = dz / dist;

        Vec3 ta = MinecartMath.forward(a);
        Vec3 tb = MinecartMath.forward(b);
        double gA = ta.x * nx + ta.z * nz;
        double gB = tb.x * nx + tb.z * nz;
        double denom = Math.max(gA * gA + gB * gB, MIN_DENOM);

        Vec3 va = a.getDeltaMovement();
        Vec3 vb = b.getDeltaMovement();
        double speed = Math.max(va.horizontalDistance(), vb.horizontalDistance());

        if (dist < REST) return;

        double error = REST - dist;
        if (error < -POS_SLOP) {
            double maxPosFix = Math.max(0.5, speed * 1.5);
            double lambda = Mth.clamp(error, -maxPosFix, maxPosFix) / denom;

            Vec3 moveA = ta.scale(-lambda * gA);
            Vec3 moveB = tb.scale(lambda * gB);
            Vec3 beforeA = a.position();
            Vec3 beforeB = b.position();

            a.move(MoverType.SELF, moveA);
            b.move(MoverType.SELF, moveB);

            boolean blockedA = moveA.subtract(a.position().subtract(beforeA)).lengthSqr() > 1.0E-8;
            boolean blockedB = moveB.subtract(b.position().subtract(beforeB)).lengthSqr() > 1.0E-8;
            if (blockedA) b.setDeltaMovement(0.0, b.getDeltaMovement().y, 0.0);
            if (blockedB) a.setDeltaMovement(0.0, a.getDeltaMovement().y, 0.0);

            va = a.getDeltaMovement();
            vb = b.getDeltaMovement();
        }

        double rel = (vb.x - va.x) * nx + (vb.z - va.z) * nz;
        double maxVelFix = Math.max(0.3, speed);
        double delta = Mth.clamp(-rel, -maxVelFix, maxVelFix);
        if (Math.abs(delta) > 1.0E-6) {
            double lambda = delta / denom;
            a.setDeltaMovement(keepSlope(va, capHorizontalSpeed(new Vec3(va.x - ta.x * lambda * gA, va.y, va.z - ta.z * lambda * gA), speedCap)));
            b.setDeltaMovement(keepSlope(vb, capHorizontalSpeed(new Vec3(vb.x + tb.x * lambda * gB, vb.y, vb.z + tb.z * lambda * gB), speedCap)));
        }
    }

    @Unique
    private static void solveFreeLink(AbstractMinecart a, AbstractMinecart b, double speedCap) {
        double wA = a.isOnRails() ? 0.0 : 1.0;
        double wB = b.isOnRails() ? 0.0 : 1.0;
        double wSum = wA + wB;
        if (wSum == 0.0) return;

        double dx = b.getX() - a.getX();
        double dz = b.getZ() - a.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0E-4) return;
        double nx = dx / dist, nz = dz / dist;

        Vec3 va = a.getDeltaMovement();
        Vec3 vb = b.getDeltaMovement();

        if (dist < REST) return;

        double error = REST - dist;
        if (error < -POS_SLOP) {
            double fix = Mth.clamp(error, -0.5, 0.5);
            a.move(MoverType.SELF, new Vec3(-nx * fix * wA / wSum, 0.0, -nz * fix * wA / wSum));
            b.move(MoverType.SELF, new Vec3(nx * fix * wB / wSum, 0.0, nz * fix * wB / wSum));
            va = a.getDeltaMovement();
            vb = b.getDeltaMovement();
        }

        double rel = (vb.x - va.x) * nx + (vb.z - va.z) * nz;
        if (rel > 1.0E-6) {
            a.setDeltaMovement(capHorizontalSpeed(new Vec3(va.x + nx * rel * wA / wSum, va.y, va.z + nz * rel * wA / wSum), speedCap));
            b.setDeltaMovement(capHorizontalSpeed(new Vec3(vb.x - nx * rel * wB / wSum, vb.y, vb.z - nz * rel * wB / wSum), speedCap));
        }
    }

    @Unique
    private static Vec3 capHorizontalSpeed(Vec3 v, double max) {
        double horizontalSpeed = v.horizontalDistance();
        if (horizontalSpeed <= max) return v;
        double s = max / horizontalSpeed;
        return new Vec3(v.x * s, v.y, v.z * s);
    }

    @Unique
    private static Vec3 keepSlope(Vec3 old, Vec3 updated) {
        double oldSq = old.x * old.x + old.z * old.z;
        if (oldSq < 1.0E-8 || Math.abs(old.y) < 1.0E-6) return updated;
        double ratio = Math.min(1.0, (updated.x * old.x + updated.z * old.z) / oldSq);
        return new Vec3(updated.x, old.y * ratio, updated.z);
    }

    @Inject(method = "getDefaultGravity", at = @At("HEAD"), cancellable = true)
    private void updateGravity(CallbackInfoReturnable<Double> cir) {
        float base = 0.08f;
        cir.setReturnValue(this.isInWater() ? base * 0.8 : base);
    }

    @Inject(method = "getAirDrag", at = @At("HEAD"), cancellable = true)
    private void updateAirDrag(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.99f);
    }

    @Inject(method = "isFlipped", at = @At("HEAD"), cancellable = true)
    private void what_we_missed$neverFlipped(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "setFlipped", at = @At("HEAD"), cancellable = true)
    private void what_we_missed$ignoreFlip(boolean flipped, CallbackInfo ci) {
        ci.cancel();
    }

    @WrapMethod(method = "useExperimentalMovement")
    private static boolean useNewMovement(Level level, Operation<Boolean> original) {
        return true;
    }
}

