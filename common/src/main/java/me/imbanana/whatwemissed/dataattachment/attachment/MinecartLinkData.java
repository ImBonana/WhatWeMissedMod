package me.imbanana.whatwemissed.dataattachment.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
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

    public enum ChainType implements StringRepresentable {
        IRON("iron"),
        COPPER("copper"),
        COPPER_EXPOSED("exposed_copper"),
        COPPER_WEATHERED("weathered_copper"),
        COPPER_OXIDIZED("oxidized_copper");

        public static final Codec<ChainType> CODEC = StringRepresentable.fromEnum(ChainType::values);
        public static final StreamCodec<ByteBuf, ChainType> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ChainType::getSerializedName, ChainType::fromName);

        private final String name;

        ChainType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static ChainType fromName(String name) {
            return Arrays.stream(values()).filter(chainType -> chainType.name.equals(name)).findFirst().orElse(null);
        }
    }
}

