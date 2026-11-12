package me.imbanana.whatwemissed.network.payload;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManager;
import me.imbanana.whatwemissed.util.ChainedCartLinkSide;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record UnlinkMinecartPayloadC2S(int cartId, ChainedCartLinkSide side) implements CustomPacketPayload {
    public static final Identifier ID = WhatWeMissed.idOf("unlink_minecart");
    public static final Type<UnlinkMinecartPayloadC2S> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, UnlinkMinecartPayloadC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UnlinkMinecartPayloadC2S::cartId,
            ChainedCartLinkSide.STREAM_CODEC,
            UnlinkMinecartPayloadC2S::side,
            UnlinkMinecartPayloadC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(UnlinkMinecartPayloadC2S payload, ServerPlayer player) {
        MinecartLinkManager.handleUnlink(payload, player);
    }
}
