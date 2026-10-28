package me.imbanana.whatwemissed.dataattachment.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public record MinecartLinkData(Optional<UUID> uuid, MinecartLinkManager.ChainType chainType) {
    public static final MinecartLinkData EMPTY = new MinecartLinkData(Optional.empty(), MinecartLinkManager.ChainType.IRON);

    public static final Codec<MinecartLinkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("target").forGetter(MinecartLinkData::uuid),
            MinecartLinkManager.ChainType.CODEC.fieldOf("chainType").forGetter(MinecartLinkData::chainType)
    ).apply(instance, MinecartLinkData::new));

    public static final StreamCodec<ByteBuf, MinecartLinkData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            MinecartLinkData::uuid,
            MinecartLinkManager.ChainType.STREAM_CODEC,
            MinecartLinkData::chainType,
            MinecartLinkData::new
    );

}

