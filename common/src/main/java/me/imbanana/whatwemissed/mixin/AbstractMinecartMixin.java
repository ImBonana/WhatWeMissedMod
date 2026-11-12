package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.imbanana.whatwemissed.dataattachment.ModDataAttachmentTypes;
import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import me.imbanana.whatwemissed.managers.minecart.train.TrainChain;
import me.imbanana.whatwemissed.managers.minecart.train.TrainState;
import me.imbanana.whatwemissed.util.ChainType;
import me.imbanana.whatwemissed.util.ChainedCartLinkSide;
import me.imbanana.whatwemissed.util.CustomMinecartBehavior;
import me.imbanana.whatwemissed.util.IAbstractMinecart;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity implements IAbstractMinecart {
    @Mutable
    @Shadow
    @Final
    private MinecartBehavior behavior;

    @Shadow
    public abstract void tick();

    @Unique
    private long what_we_missed$trainTick = -1;
    @Unique
    private TrainState what_we_missed$trainState;

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

    @Unique
    private static MinecartLinkData what_we_missed$getLinkData(Entity entity) {
        return ModDataAttachmentTypes.MINECART_LINK.getOrElse(entity, MinecartLinkData.EMPTY);
    }

    @Unique
    private static void what_we_missed$setData(Entity entity, MinecartLinkData data) {
        if (data.isEmpty()) {
            if (ModDataAttachmentTypes.MINECART_LINK.has(entity)) {
                ModDataAttachmentTypes.MINECART_LINK.remove(entity);
            }
        } else if (!data.equals(ModDataAttachmentTypes.MINECART_LINK.get(entity))) {
            ModDataAttachmentTypes.MINECART_LINK.set(entity, data);
        }
    }

    @Unique
    private AbstractMinecart what_we_missed$getCartEntity(UUID uuid) {
        return this.level().getEntity(uuid) instanceof AbstractMinecart minecart && minecart.isAlive() ? minecart : null;
    }

    @Override
    public AbstractMinecart what_we_missed$getParent() {
        Optional<MinecartLinkData.ParentLink> link = what_we_missed$getLinkData(this).parent();
        if (link.isEmpty()) return null;

        AbstractMinecart parent = this.what_we_missed$getCartEntity(link.get().uuid());
        if (parent == null) return null;
        return this.getUUID().equals(what_we_missed$getLinkData(parent).child().orElse(null)) ? parent : null;
    }

    @Override
    public AbstractMinecart what_we_missed$getChild() {
        Optional<UUID> link = what_we_missed$getLinkData(this).child();
        if (link.isEmpty()) return null;

        AbstractMinecart child = this.what_we_missed$getCartEntity(link.get());
        if (child == null) return null;
        Optional<MinecartLinkData.ParentLink> back = what_we_missed$getLinkData(child).parent();
        return back.isPresent() && back.get().uuid().equals(this.getUUID()) ? child : null;
    }

    @Override
    public ChainedCartLinkSide what_we_missed$getParentSide() {
        return what_we_missed$getLinkData(this).parentSide();
    }

    @Override
    public ChainedCartLinkSide what_we_missed$getChildSide() {
        return what_we_missed$getLinkData(this).childSide();
    }

    @Override
    public ChainType what_we_missed$getParentChainType() {
        return what_we_missed$getLinkData(this).parent().map(MinecartLinkData.ParentLink::chainType).orElse(ChainType.IRON);
    }

    @Override
    public boolean what_we_missed$linkChild(ChainedCartLinkSide thisSide, AbstractMinecart child, ChainedCartLinkSide childSide, ChainType chainType) {
        if (child == (Object) this || this.what_we_missed$getChild() != null || child.what_we_missed$getParent() != null) return false;

        what_we_missed$setData(this, what_we_missed$getLinkData(this).withChild(child.getUUID(), thisSide));
        what_we_missed$setData(child, what_we_missed$getLinkData(child).withParent(new MinecartLinkData.ParentLink(this.getUUID(), chainType), childSide));
        return true;
    }

    @Override
    public void what_we_missed$unlinkParent() {
        MinecartLinkData data = what_we_missed$getLinkData(this);
        if (data.parent().isEmpty()) return;

        if (this.level() instanceof ServerLevel level) {
            this.spawnAtLocation(level, new ItemStack(data.parent().get().chainType().getDropItem(), 1));
        }

        AbstractMinecart parent = this.what_we_missed$getParent();
        what_we_missed$setData(this, data.withoutParent());
        if (parent != null) what_we_missed$setData(parent, what_we_missed$getLinkData(parent).withoutChild());
    }

    @Override
    public void what_we_missed$unlinkChild() {
        AbstractMinecart child = this.what_we_missed$getChild();
        if (child != null) {
            child.what_we_missed$unlinkParent();
            return;
        }

        MinecartLinkData data = what_we_missed$getLinkData(this);
        if (data.child().isPresent()) what_we_missed$setData(this, data.withoutChild());
    }

    @Override
    public void what_we_missed$turnTrainAround() {
        List<AbstractMinecart> carts = TrainChain.of((AbstractMinecart) (Object) this).carts();
        int trainSize = carts.size();
        if (trainSize < 2) return;

        ChainType[] chainTypes = new ChainType[trainSize];
        ChainedCartLinkSide[] parentSides = new ChainedCartLinkSide[trainSize];
        ChainedCartLinkSide[] childSides = new ChainedCartLinkSide[trainSize];
        for (int i = 0; i < trainSize; i++) {
            chainTypes[i] = carts.get(i).what_we_missed$getParentChainType();
            parentSides[i] = carts.get(i).what_we_missed$getParentSide();
            childSides[i] = carts.get(i).what_we_missed$getChildSide();
        }

        for (int i = 0; i < trainSize; i++) {
            Optional<MinecartLinkData.ParentLink> parent = i < trainSize - 1
                    ? Optional.of(new MinecartLinkData.ParentLink(carts.get(i + 1).getUUID(), chainTypes[i + 1]))
                    : Optional.empty();
            Optional<UUID> child = i > 0 ? Optional.of(carts.get(i - 1).getUUID()) : Optional.empty();
            what_we_missed$setData(carts.get(i), new MinecartLinkData(parent, child, childSides[i], parentSides[i]));
        }
    }

    @Override
    public long what_we_missed$getTrainTick() {
        return what_we_missed$trainTick;
    }

    @Override
    public void what_we_missed$setTrainTick(long tick) {
        what_we_missed$trainTick = tick;
    }

    @Override
    public TrainState what_we_missed$getTrainState() {
        return what_we_missed$trainState;
    }

    @Override
    public void what_we_missed$setTrainState(TrainState state) {
        what_we_missed$trainState = state;
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
