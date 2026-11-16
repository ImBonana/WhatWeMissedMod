package me.imbanana.whatwemissed.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.WeatheringCopper;

import java.util.StringJoiner;

public record CopperStateDataComponent(WeatheringCopper.WeatherState state, boolean waxed) {
    public static final CopperStateDataComponent DEFAULT = new CopperStateDataComponent(WeatheringCopper.WeatherState.UNAFFECTED, false);

    public static final Codec<CopperStateDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeatheringCopper.WeatherState.CODEC.fieldOf("state").forGetter(CopperStateDataComponent::state),
            Codec.BOOL.fieldOf("waxed").forGetter(CopperStateDataComponent::waxed)
    ).apply(instance, CopperStateDataComponent::new));

    public static final StreamCodec<FriendlyByteBuf, CopperStateDataComponent> STREAM_CODEC = StreamCodec.composite(
            WeatheringCopper.WeatherState.STREAM_CODEC, CopperStateDataComponent::state,
            ByteBufCodecs.BOOL, CopperStateDataComponent::waxed,
            CopperStateDataComponent::new
    );

    public String getTranslationSuffix() {
        StringJoiner suffix = new StringJoiner("_");
        if (waxed) suffix.add("waxed");
        if (state != WeatheringCopper.WeatherState.UNAFFECTED) suffix.add(state.getSerializedName());
        return suffix.toString();
    }

    public String getTranslationKey(String baseKey) {
        String suffix = this.getTranslationSuffix();
        return suffix.isEmpty() ? baseKey : baseKey + "." + suffix;
    }
}
