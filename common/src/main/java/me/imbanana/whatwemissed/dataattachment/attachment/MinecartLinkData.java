package me.imbanana.whatwemissed.dataattachment.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import me.imbanana.whatwemissed.util.ChainType;
import me.imbanana.whatwemissed.util.ChainedCartLinkSide;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public record MinecartLinkData(Optional<UUID> uuid, ChainType chainType, ChainedCartLinkSide thisSide, ChainedCartLinkSide otherSide) {
    public static final MinecartLinkData EMPTY = new MinecartLinkData(Optional.empty(), ChainType.IRON, ChainedCartLinkSide.BACK, ChainedCartLinkSide.FRONT);

    public static final Codec<MinecartLinkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("target").forGetter(MinecartLinkData::uuid),
            ChainType.CODEC.fieldOf("chainType").forGetter(MinecartLinkData::chainType),
            ChainedCartLinkSide.CODEC.fieldOf("thisSide").forGetter(MinecartLinkData::thisSide),
            ChainedCartLinkSide.CODEC.fieldOf("otherSide").forGetter(MinecartLinkData::otherSide)
    ).apply(instance, MinecartLinkData::new));

    public static final StreamCodec<ByteBuf, MinecartLinkData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            MinecartLinkData::uuid,
            ChainType.STREAM_CODEC,
            MinecartLinkData::chainType,
            ChainedCartLinkSide.STREAM_CODEC,
            MinecartLinkData::thisSide,
            ChainedCartLinkSide.STREAM_CODEC,
            MinecartLinkData::otherSide,
            MinecartLinkData::new
    );
}

