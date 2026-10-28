package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.dataattachment.ModDataAttachmentTypes;
import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManager;
import me.imbanana.whatwemissed.util.CustomMinecartBehavior;
import me.imbanana.whatwemissed.util.IAbstractMinecart;
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
    private static final double REST = 1.8;
    @Unique
    private static final double MIN_DIST = 2;
    @Unique
    private static final double STIFFNESS = 0.3;
    @Unique
    private static final double DAMPING = 0.8;
    @Unique
    private static final double MAX_IMPULSE = 0.3;
    @Unique
    private static final double MAX_SPEED = 0.4;
    @Unique
    private static final double BREAK_DIST = REST * 2.5;

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
    public void what_we_missed$link(AbstractMinecart minecart, MinecartLinkManager.ChainType chainType) {
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
    public MinecartLinkManager.ChainType what_we_missed$getChainType() {
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

        if (dist > BREAK_DIST) {
            this.what_we_missed$unlink();
            return;
        }

        double nx = dx / dist;
        double nz = dz / dist;
        Vec3 va = this.getDeltaMovement();
        Vec3 vb = other.getDeltaMovement();
        double relSpeed = (vb.x - va.x) * nx + (vb.z - va.z) * nz;

        double impulse = 0.0;

        if (dist > REST) {
            impulse = (dist - REST) * STIFFNESS + relSpeed * DAMPING;
            impulse = Mth.clamp(impulse, 0.0, MAX_IMPULSE);
        } else if (dist < MIN_DIST && relSpeed < 0) {
            impulse = Math.max(relSpeed, -MAX_IMPULSE * 2.0);
        }

        if (impulse == 0.0) return;

        double half = impulse * 0.5;
        apply(self, va, nx * half, nz * half);
        apply(other, vb, -nx * half, -nz * half);
    }

    @Unique
    private static void apply(AbstractMinecart cart, Vec3 v, double ix, double iz) {
        Vec3 axis = Vec3.directionFromRotation(0f, cart.getYRot() + 90f);
        double along = ix * axis.x + iz * axis.z;

        double vx = v.x + axis.x * along;
        double vz = v.z + axis.z * along;

        double h = Math.hypot(vx, vz);
        if (h > MAX_SPEED) {
            vx *= MAX_SPEED / h;
            vz *= MAX_SPEED / h;
        }

        cart.setDeltaMovement(vx, v.y, vz);
    }

    @Inject(method = "isFlipped", at = @At("HEAD"), cancellable = true)
    private void what_we_missed$neverFlipped(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "setFlipped", at = @At("HEAD"), cancellable = true)
    private void what_we_missed$ignoreFlip(boolean flipped, CallbackInfo ci) {
        ci.cancel();
    }
}

