package me.imbanana.whatwemissed.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MinecartFurnaceFuelDataComponent(int burnTime, int speed) {
    public static final Codec<MinecartFurnaceFuelDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("burnTime").forGetter(MinecartFurnaceFuelDataComponent::burnTime),
            Codec.INT.fieldOf("speed").forGetter(MinecartFurnaceFuelDataComponent::speed)
    ).apply(instance, MinecartFurnaceFuelDataComponent::new));

    public static final StreamCodec<FriendlyByteBuf, MinecartFurnaceFuelDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MinecartFurnaceFuelDataComponent::burnTime,
            ByteBufCodecs.VAR_INT, MinecartFurnaceFuelDataComponent::speed,
            MinecartFurnaceFuelDataComponent::new
    );

    public double getSpeedInBlocksPerTick() {
        return speed / 20f;
    }
}
