package me.imbanana.whatwemissed.dataattachment.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import me.imbanana.whatwemissed.util.ChainType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public record MinecartLinkData(Optional<UUID> uuid, ChainType chainType) {
    public static final MinecartLinkData EMPTY = new MinecartLinkData(Optional.empty(), ChainType.IRON);

    public static final Codec<MinecartLinkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("target").forGetter(MinecartLinkData::uuid),
            ChainType.CODEC.fieldOf("chainType").forGetter(MinecartLinkData::chainType)
    ).apply(instance, MinecartLinkData::new));

    public static final StreamCodec<ByteBuf, MinecartLinkData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            MinecartLinkData::uuid,
            ChainType.STREAM_CODEC,
            MinecartLinkData::chainType,
            MinecartLinkData::new
    );

}

