package me.imbanana.whatwemissed.entity.custom;

import me.imbanana.whatwemissed.item.ModDataComponents;
import me.imbanana.whatwemissed.item.ModItems;
import me.imbanana.whatwemissed.item.components.CopperStateDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class MinecartCopperChestEntity extends MinecartChest {
    private static final EntityDataAccessor<WeatheringCopper.WeatherState> DATA_WEATHER_STATE = SynchedEntityData.defineId(
            MinecartCopperChestEntity.class, EntityDataSerializers.WEATHERING_COPPER_STATE
    );

    private static final EntityDataAccessor<Boolean> DATA_WAXED = SynchedEntityData.defineId(
            MinecartCopperChestEntity.class, EntityDataSerializers.BOOLEAN
    );

    private long nextWeatheringTick = -1L;
    private @Nullable UUID lastLightningBoltUUID;

    public MinecartCopperChestEntity(EntityType<? extends MinecartChest> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_WEATHER_STATE, WeatheringCopper.WeatherState.UNAFFECTED);
        entityData.define(DATA_WAXED, false);
    }

    @Override
    public void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putLong("next_weather_age", this.nextWeatheringTick);
        output.putBoolean("waxed", this.isWaxed());
        output.store("weather_state", WeatheringCopper.WeatherState.CODEC, this.getWeatherState());
    }

    @Override
    public void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);
        this.nextWeatheringTick = input.getLongOr("next_weather_age", -1L);
        this.setWaxed(input.getBooleanOr("waxed", false));
        this.setWeatherState(input.read("weather_state", WeatheringCopper.WeatherState.CODEC).orElse(WeatheringCopper.WeatherState.UNAFFECTED));
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(this.getCopperState().getTranslationKey(this.getType().getDescriptionId()));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.updateWeathering(this.level().getRandom(), this.level().getGameTime());
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack itemStack = player.getItemInHand(hand);

        Level level = this.level();

        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        if (itemStack.is(Items.HONEYCOMB) && !this.isWaxed()) {
            BlockPos pos = this.blockPosition();
            level.levelEvent(this, 3003, pos, 0);
            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.setWaxed(true);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (itemStack.is(ItemTags.AXES) && this.isWaxed()) {
            level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
            level.levelEvent(this, 3004, this.blockPosition(), 0);
            this.nextWeatheringTick = -1L;
            this.setWaxed(false);
            itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            return InteractionResult.SUCCESS_SERVER;
        }

        if (itemStack.is(ItemTags.AXES)) {
            WeatheringCopper.WeatherState weatherState = this.getWeatherState();
            if (weatherState != WeatheringCopper.WeatherState.UNAFFECTED) {
                level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
                level.levelEvent(this, 3005, this.blockPosition(), 0);
                this.nextWeatheringTick = -1L;
                this.entityData.set(DATA_WEATHER_STATE, weatherState.previous(), true);
                itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.interact(player, hand, location);
    }

    private void updateWeathering(final RandomSource random, final long gameTime) {
        if (this.isWaxed()) return;

        if (this.nextWeatheringTick == -1L) {
            this.nextWeatheringTick = gameTime + random.nextIntBetweenInclusive(504000, 552000);
        } else {
            WeatheringCopper.WeatherState weatherState = this.entityData.get(DATA_WEATHER_STATE);
            boolean isFullyOxidized = weatherState.equals(WeatheringCopper.WeatherState.OXIDIZED);
            if (gameTime >= this.nextWeatheringTick && !isFullyOxidized) {
                WeatheringCopper.WeatherState newState = weatherState.next();
                boolean isNewStateFullyOxidized = newState.equals(WeatheringCopper.WeatherState.OXIDIZED);
                this.setWeatherState(newState);
                this.nextWeatheringTick = isNewStateFullyOxidized ? 0L : this.nextWeatheringTick + random.nextIntBetweenInclusive(504000, 552000);
            }
        }
    }

    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightningBolt) {
        super.thunderHit(level, lightningBolt);
        UUID lightningBoltUUID = lightningBolt.getUUID();
        if (!lightningBoltUUID.equals(this.lastLightningBoltUUID)) {
            this.lastLightningBoltUUID = lightningBoltUUID;
            WeatheringCopper.WeatherState weatherState = this.getWeatherState();
            if (weatherState != WeatheringCopper.WeatherState.UNAFFECTED) {
                this.nextWeatheringTick = -1L;
                this.entityData.set(DATA_WEATHER_STATE, weatherState.previous(), true);
            }
        }
    }

    public WeatheringCopper.WeatherState getWeatherState() {
        return this.entityData.get(DATA_WEATHER_STATE);
    }

    public void setWeatherState(final WeatheringCopper.WeatherState state) {
        this.entityData.set(DATA_WEATHER_STATE, state);
    }

    public boolean isWaxed() {
        return this.entityData.get(DATA_WAXED);
    }

    public void setWaxed(boolean waxed) {
        this.entityData.set(DATA_WAXED, waxed);
    }

    private CopperStateDataComponent getCopperState() {
        return new CopperStateDataComponent(this.getWeatherState(), this.isWaxed());
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        WeatheringCopperCollection.ByState<Block> copperCollection = this.isWaxed() ? Blocks.COPPER_CHEST.waxed() : Blocks.COPPER_CHEST.weathering();
        return copperCollection.pick(this.getWeatherState()).defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
    }

    @Override
    public void destroy(final ServerLevel level, final Item dropItem) {
        this.kill(level);
        if (level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            ItemStack itemStack = new ItemStack(dropItem);
            itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
            itemStack.set(ModDataComponents.COPPER_STATE.get(), this.getCopperState());
            this.spawnAtLocation(level, itemStack);
        }
    }

    @Override
    protected Item getDropItem() {
        return ModItems.COPPER_CHEST_MINECART;
    }


    @Override
    public ItemStack getPickResult() {
        ItemStack itemStack = new ItemStack(this.getDropItem());
        itemStack.set(ModDataComponents.COPPER_STATE.get(), this.getCopperState());
        return itemStack;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.applyImplicitComponentIfPresent(components, ModDataComponents.COPPER_STATE.get());
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> type, T value) {
        boolean result = super.applyImplicitComponent(type, value);
        if (result) return result;

        if (type == ModDataComponents.COPPER_STATE.get()) {
            CopperStateDataComponent copperState = castComponentValue(ModDataComponents.COPPER_STATE.get(), value);
            this.setWeatherState(copperState.state());
            this.setWaxed(copperState.waxed());
            return true;
        }

        return false;
    }
}
