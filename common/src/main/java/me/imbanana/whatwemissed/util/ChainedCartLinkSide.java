package me.imbanana.whatwemissed.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum ChainedCartLinkSide implements StringRepresentable {
    FRONT("front", 0),
    BACK("back", 1);

    public static final Codec<ChainedCartLinkSide> CODEC = StringRepresentable.fromEnum(ChainedCartLinkSide::values);
    public static final StreamCodec<ByteBuf, ChainedCartLinkSide> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(
                    ChainedCartLinkSide::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO
            ),
            ChainedCartLinkSide::getId
    );

    private final String name;
    private final int id;

    ChainedCartLinkSide(String name, int id) {
        this.name = name;
        this.id = id;
    }

    private int getId() {
        return id;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
