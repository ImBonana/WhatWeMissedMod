package me.imbanana.whatwemissed.network.payload;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public record LinkMinecartPayloadC2S(int firstCartId, int secondCartId, InteractionHand hand) implements CustomPacketPayload {
    public static final Identifier ID = WhatWeMissed.ifOd("link_minecart");
    public static final Type<LinkMinecartPayloadC2S> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, LinkMinecartPayloadC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            LinkMinecartPayloadC2S::firstCartId,
            ByteBufCodecs.VAR_INT,
            LinkMinecartPayloadC2S::secondCartId,
            InteractionHand.STREAM_CODEC,
            LinkMinecartPayloadC2S::hand,
            LinkMinecartPayloadC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(LinkMinecartPayloadC2S payload, ServerPlayer player) {
        MinecartLinkManager.handleLink(payload, player);
    }
}
