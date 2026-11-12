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

public record MinecartLinkData(Optional<ParentLink> parent, Optional<UUID> child, ChainedCartLinkSide parentSide, ChainedCartLinkSide childSide) {
    public static final MinecartLinkData EMPTY = new MinecartLinkData(Optional.empty(), Optional.empty(), ChainedCartLinkSide.FRONT, ChainedCartLinkSide.BACK);

    public record ParentLink(UUID uuid, ChainType chainType) {
        public static final Codec<ParentLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(ParentLink::uuid),
                ChainType.CODEC.fieldOf("chainType").forGetter(ParentLink::chainType)
        ).apply(instance, ParentLink::new));

        public static final StreamCodec<ByteBuf, ParentLink> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ParentLink::uuid,
                ChainType.STREAM_CODEC,
                ParentLink::chainType,
                ParentLink::new
        );
    }

    public static final Codec<MinecartLinkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParentLink.CODEC.optionalFieldOf("parent").forGetter(MinecartLinkData::parent),
            UUIDUtil.CODEC.optionalFieldOf("child").forGetter(MinecartLinkData::child),
            ChainedCartLinkSide.CODEC.optionalFieldOf("parentSide", ChainedCartLinkSide.FRONT).forGetter(MinecartLinkData::parentSide),
            ChainedCartLinkSide.CODEC.optionalFieldOf("childSide", ChainedCartLinkSide.BACK).forGetter(MinecartLinkData::childSide)
    ).apply(instance, MinecartLinkData::new));

    public static final StreamCodec<ByteBuf, MinecartLinkData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ParentLink.STREAM_CODEC),
            MinecartLinkData::parent,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            MinecartLinkData::child,
            ChainedCartLinkSide.STREAM_CODEC,
            MinecartLinkData::parentSide,
            ChainedCartLinkSide.STREAM_CODEC,
            MinecartLinkData::childSide,
            MinecartLinkData::new
    );

    public MinecartLinkData withParent(ParentLink parent, ChainedCartLinkSide side) {
        return new MinecartLinkData(Optional.of(parent), this.child, side, this.childSide);
    }

    public MinecartLinkData withoutParent() {
        return new MinecartLinkData(Optional.empty(), this.child, this.parentSide, this.childSide);
    }

    public MinecartLinkData withChild(UUID child, ChainedCartLinkSide side) {
        return new MinecartLinkData(this.parent, Optional.of(child), this.parentSide, side);
    }

    public MinecartLinkData withoutChild() {
        return new MinecartLinkData(this.parent, Optional.empty(), this.parentSide, this.childSide);
    }

    public boolean isEmpty() {
        return this.parent.isEmpty() && this.child.isEmpty();
    }
}
