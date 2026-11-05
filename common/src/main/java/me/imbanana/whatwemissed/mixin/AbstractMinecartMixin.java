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

import java.util.Optional;

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
    private static final double MAX_VEL_FIX = 0.3;
    @Unique
    private static final double MAX_POS_FIX = 0.75;
    @Unique
    private static final double POS_SLOP = 0.002;

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

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void solveLink(CallbackInfo ci) {
        if (this.level().isClientSide()) return;

        AbstractMinecart self = (AbstractMinecart) (Object) this;
        AbstractMinecart other = this.what_we_missed$getLinkedEntity();
        if (other == null) return;

        double dx = other.getX() - this.getX();
        double dz = other.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0E-4) return;

        double nx = dx / dist;
        double nz = dz / dist;

        Vec3 ta = MinecartMath.forward(self);
        Vec3 tb = MinecartMath.forward(other);
        double gA = ta.x * nx + ta.z * nz;
        double gB = tb.x * nx + tb.z * nz;
        double denom = gA * gA + gB * gB;
        if (denom < 1.0E-3) return;

        double error = REST - dist;
        if (Math.abs(error) > POS_SLOP) {
            double e = Mth.clamp(error, -MAX_POS_FIX, MAX_POS_FIX);
            double lambda = e / denom;
            double sA = -lambda * gA;
            double sB = lambda * gB;
            self.setPos(self.getX() + ta.x * sA, self.getY(), self.getZ() + ta.z * sA);
            other.setPos(other.getX() + tb.x * sB, other.getY(), other.getZ() + tb.z * sB);
        }

        Vec3 velA = this.getDeltaMovement();
        Vec3 velB = other.getDeltaMovement();
        double rel = (velB.x - velA.x) * nx + (velB.z - velA.z) * nz;
        double delta = Mth.clamp(-rel, -MAX_VEL_FIX, MAX_VEL_FIX);
        if (Math.abs(delta) > 1.0E-6) {
            double lambda = delta / denom;
            double dA = -lambda * gA;
            double dB = lambda * gB;
            self.setDeltaMovement(velA.x + ta.x * dA, velA.y, velA.z + ta.z * dA);
            other.setDeltaMovement(velB.x + tb.x * dB, velB.y, velB.z + tb.z * dB);
        };
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

