package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.imbanana.whatwemissed.item.ModDataComponents;
import me.imbanana.whatwemissed.item.components.MinecartFurnaceFuelDataComponent;
import me.imbanana.whatwemissed.screen.minecartfurnace.MinecartFurnaceMenu;
import me.imbanana.whatwemissed.util.MinecartMath;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
public abstract class MinecartFurnaceMixin extends AbstractMinecart implements ContainerEntity {
    @Shadow
    private int fuel;
    @Shadow
    public Vec3 push;

    @Shadow
    protected abstract void setHasFuel(boolean fuel);

    @Shadow
    protected abstract boolean hasFuel();

    @Shadow
    protected abstract Vec3 calculateNewPushAlong(Vec3 deltaMovement);

    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);

    @Unique
    private int totalFuel = 0;
    @Unique
    private boolean enabled = true;
    @Unique
    private double maxSpeed; // In b/t

    @Unique
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0 -> fuel;
                case 1 -> totalFuel;
                case 2 -> enabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int dataId, int value) {
            switch (dataId) {
                case 0 -> fuel = value;
                case 1 -> totalFuel = value;
                case 2 -> enabled = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    protected MinecartFurnaceMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    // Container Setup
    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MinecartFurnaceMenu(containerId, inventory, this, this.containerData);
    }

    @Override
    protected void destroy(ServerLevel level, DamageSource source) {
        super.destroy(level, source);
        this.chestVehicleDestroyed(source, level, this);
    }
    @Override
    public ItemStack getItem(int slot) { return this.getChestVehicleItem(slot);}
    @Override
    public ItemStack removeItem(int slot, int count) { return this.removeChestVehicleItem(slot, count); }
    @Override
    public ItemStack removeItemNoUpdate(int slot) { return this.removeChestVehicleItemNoUpdate(slot); }
    @Override
    public void setItem(int slot, ItemStack itemStack) { this.setChestVehicleItem(slot, itemStack); }
    @Override
    public @Nullable SlotAccess getSlot(int slot) { return this.getChestVehicleSlot(slot); }
    @Override
    public void setChanged() { }
    @Override
    public boolean stillValid(Player player) { return this.isChestVehicleStillValid(player); }
    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide() && reason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }

        super.remove(reason);
    }
    @Override
    public void clearContent() { this.clearChestVehicleContent(); }
    @Override
    public NonNullList<ItemStack> getItemStacks() { return itemStacks; }
    @Override
    public void clearItemStacks() { this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY); }
    @Override
    public int getContainerSize() { return itemStacks.size(); }
    @Override
    public @Nullable ResourceKey<LootTable> getContainerLootTable() { return null; }
    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> lootTable) { }
    @Override
    public long getContainerLootTableSeed() { return 0; }
    @Override
    public void setContainerLootTableSeed(long lootTableSeed) { }


    @Override
    public void activateMinecart(ServerLevel level, int xt, int yt, int zt, boolean state) {
        this.enabled = state;
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void addVehicleSavedData(ValueOutput output, CallbackInfo ci) {
        output.putShort("totalFuelTime", (short) this.totalFuel);
        output.putBoolean("isLit", this.enabled);
        output.putDouble("maxSpeed", this.maxSpeed);
        this.addChestVehicleSaveData(output);
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void readVehicleSavedData(ValueInput input, CallbackInfo ci) {
        this.totalFuel = input.getShortOr("totalFuelTime", (short) 0);
        this.enabled = input.getBooleanOr("isLit", true);
        this.maxSpeed = input.getDoubleOr("maxSpeed", 0);
        this.readChestVehicleSaveData(input);
    }

    @Inject(
            method = "interact",
            at = @At("HEAD"),
            cancellable = true
    )
    private void openContainer(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        cir.setReturnValue(this.interactWithContainerVehicle(player));
        cir.cancel();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;tick()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onTick(CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            if (this.fuel > 0 && this.enabled) {
                this.fuel--;
            }

            if (this.fuel <= 0 && this.enabled) {
                this.consumeFuel();
            }

            boolean isBurning = this.fuel > 0 && this.enabled;
            this.setHasFuel(isBurning);

            if (isBurning) {
                if (this.push.lengthSqr() < 1.0E-7) {
                    Vec3 vel = this.getDeltaMovement().horizontal();
                    this.push = vel.lengthSqr() > 0.0009 ? vel.normalize() : this.getPushDirection();
                } else if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0009) {
                    this.push = this.getPushDirection();
                }
            } else {
                this.push = Vec3.ZERO;
            }
        }

        if (this.level().isClientSide()) {
            if (this.hasFuel() && this.random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
            }
        }

        ci.cancel();
    }

    @Unique
    private Vec3 getPushDirection() {
        Vec3 axis = MinecartMath.forward(this);

        AbstractMinecart linkedCart = this.getLinkedCart();
        if (linkedCart != null) {
            Vec3 opposite = this.position().subtract(linkedCart.position());
            double along = opposite.x() * axis.x() + opposite.z() * axis.z();

            if (Math.abs(along) > 1.0E-3) {
                return axis.scale(Math.signum(along));
            }
        }

        return axis.scale(this.random.nextBoolean() ? 1 : -1);
    }

    @Unique
    private AbstractMinecart getLinkedCart() {
        AbstractMinecart parent = this.what_we_missed$getParent();
        return parent != null ? parent : this.what_we_missed$getChild();
    }

    @Inject(
            method = "applyNaturalSlowdown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyMinecartSpeed(Vec3 deltaMovement, CallbackInfoReturnable<Vec3> cir) {
        Vec3 newDeltaMovement = null;

        if (push.lengthSqr() > 1.0E-7 && deltaMovement.horizontalDistance() < this.maxSpeed) {
            this.push = this.calculateNewPushAlong(deltaMovement);

            newDeltaMovement = deltaMovement.multiply(1, 0.0, 1).add(this.push);
            double speedSqr = newDeltaMovement.horizontalDistanceSqr();

            if (speedSqr > this.maxSpeed * this.maxSpeed) {
                newDeltaMovement = newDeltaMovement.normalize().scale(this.maxSpeed);
            }

            if (this.isInWater()) {
                newDeltaMovement = newDeltaMovement.scale(0.1);
            }
        }

        cir.setReturnValue(super.applyNaturalSlowdown(newDeltaMovement == null ? deltaMovement : newDeltaMovement));
    }

    @WrapMethod(method = "getMaxSpeed")
    private double changeMaxSpeed(ServerLevel level, Operation<Double> original) {
        return super.getMaxSpeed(level);
    }

    @Unique
    private void consumeFuel() {
        ItemStack fuelItemStack = this.getItem(0);
        if (fuelItemStack.isEmpty() || !fuelItemStack.has(ModDataComponents.MINECART_FURNACE_FUEL.get())) return;

        MinecartFurnaceFuelDataComponent fuelData = fuelItemStack.get(ModDataComponents.MINECART_FURNACE_FUEL.get());

        this.startBurining(fuelData.burnTime(), fuelData.getSpeedInBlocksPerTick());

        Item fuelItem = fuelItemStack.getItem();
        ItemStackTemplate remainder = fuelItem.getCraftingRemainder();
        ItemStack newFuel = fuelItemStack;
        fuelItemStack.shrink(1);
        if (remainder != null) {
            if (fuelItemStack.isEmpty()) {
                newFuel = remainder.create();
            } else {
                Containers.dropItemStack(this.level(), this.position().x(), this.position().y(), this.position().z(), remainder.create());
            }
        }

        this.itemStacks.set(0, newFuel);
    }

    @Unique
    private void startBurining(int fuelDuration, double maxSpeed) {
        this.totalFuel = fuelDuration;
        this.fuel = fuelDuration;
        this.maxSpeed = maxSpeed;
    }
}
