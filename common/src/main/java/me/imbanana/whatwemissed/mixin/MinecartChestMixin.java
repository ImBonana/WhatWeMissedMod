package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.dataattachment.ModDataAttachmentTypes;
import me.imbanana.whatwemissed.util.IChestMinecart;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MinecartChest.class)
public abstract class MinecartChestMixin extends AbstractMinecartContainer implements IChestMinecart {
    @Unique
    private static final int OPENERS_RECHECK_INTERVAL = 5;
    @Unique
    private static final double MAX_OPENER_DISTANCE_SQR = 8.0 * 8.0;

    @Unique
    private final List<ContainerUser> what_we_missed$openers = new ArrayList<>();
    @Unique
    private final ChestLidController what_we_missed$lid = new ChestLidController();

    protected MinecartChestMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.what_we_missed$lid.shouldBeOpen(ModDataAttachmentTypes.MINECART_CHEST_LID_OPEN.getOrElse(this, false));
            this.what_we_missed$lid.tickLid();
        } else if (this.tickCount % OPENERS_RECHECK_INTERVAL == 0) {
            this.what_we_missed$removeStaleOpeners();
        }
    }

    @Override
    public void startOpen(ContainerUser containerUser) {
        if (this.level().isClientSide() || this.isRemoved()) return;

        LivingEntity entity = containerUser.getLivingEntity();
        if (entity.isSpectator() || this.what_we_missed$openers.contains(containerUser)) return;

        boolean wasClosed = this.what_we_missed$openers.isEmpty();
        this.what_we_missed$openers.add(containerUser);

        if (!(entity instanceof Player)) {
            this.level().gameEvent(GameEvent.CONTAINER_OPEN, this.position(), GameEvent.Context.of(entity));
        }

        if (wasClosed) this.what_we_missed$setLidOpen(true);
    }

    @Inject(method = "stopOpen", at = @At("HEAD"))
    private void what_we_missed$removeOpener(ContainerUser containerUser, CallbackInfo ci) {
        if (this.level().isClientSide()) return;

        if (this.what_we_missed$openers.remove(containerUser) && this.what_we_missed$openers.isEmpty()) {
            this.what_we_missed$setLidOpen(false);
        }
    }

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        this.what_we_missed$removeStaleOpeners();
        return List.copyOf(this.what_we_missed$openers);
    }

    @Override
    public float what_we_missed$getLidOpenness(float partialTick) {
        return this.what_we_missed$lid.getOpenness(partialTick);
    }

    @Unique
    private void what_we_missed$removeStaleOpeners() {
        if (this.what_we_missed$openers.isEmpty()) {
            if (ModDataAttachmentTypes.MINECART_CHEST_LID_OPEN.getOrElse(this, false)) {
                ModDataAttachmentTypes.MINECART_CHEST_LID_OPEN.set(this, false);
            }
            return;
        }

        if (this.what_we_missed$openers.removeIf(user -> !this.what_we_missed$isStillOpen(user)) && this.what_we_missed$openers.isEmpty()) {
            this.what_we_missed$setLidOpen(false);
        }
    }

    @Unique
    private boolean what_we_missed$isStillOpen(ContainerUser user) {
        LivingEntity entity = user.getLivingEntity();
        if (!entity.isAlive() || entity.isRemoved() || entity.level() != this.level()) return false;
        if (entity.distanceToSqr(this) > MAX_OPENER_DISTANCE_SQR) return false;

        if (entity instanceof Player player) {
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == this;
        }

        return true;
    }

    @Unique
    private void what_we_missed$setLidOpen(boolean open) {
        ModDataAttachmentTypes.MINECART_CHEST_LID_OPEN.set(this, open);

        SoundEvent sound;
        if (this.getDisplayBlockState().getBlock() instanceof ChestBlock chestBlock) {
            sound = open ? chestBlock.getOpenChestSound() : chestBlock.getCloseChestSound();
        } else {
            sound = open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE;
        }

        this.level().playSound(null, this.getX(), this.getY() + 0.5, this.getZ(), sound, SoundSource.BLOCKS, 0.5F, this.level().getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
