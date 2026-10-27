package me.imbanana.whatwemissed.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;

public enum FurnaceMinecartPushDirection {
    FORWARD(0, 1),
    BACKWARD(1, -1);

    public static final StreamCodec<ByteBuf, FurnaceMinecartPushDirection> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(FurnaceMinecartPushDirection::fromId, FurnaceMinecartPushDirection::getId);

    private final int id;
    private final int sign;

    FurnaceMinecartPushDirection(int id, int sign) {
        this.id = id;
        this.sign = sign;
    }

    public int getId() {
        return id;
    }

    public int getSign() {
        return sign;
    }

    public static FurnaceMinecartPushDirection fromId(int id) {
        return Arrays.stream(values()).filter(member -> member.getId() == id).findFirst().orElseThrow();
    }
}
