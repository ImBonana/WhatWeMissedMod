package me.imbanana.whatwemissed.network.payload;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.util.FurnaceMinecartPushDirection;
import me.imbanana.whatwemissed.screen.minecartfurnace.MinecartFurnaceMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SetMinecartFurnacePushDirectionPayload(FurnaceMinecartPushDirection pushDirection) implements CustomPacketPayload {
    public static final Identifier ID = WhatWeMissed.ifOd("set_minecart_furnace_push_direction");
    public static final Type<SetMinecartFurnacePushDirectionPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMinecartFurnacePushDirectionPayload> STREAM_CODEC = StreamCodec.composite(
            FurnaceMinecartPushDirection.STREAM_CODEC,
            SetMinecartFurnacePushDirectionPayload::pushDirection,
            SetMinecartFurnacePushDirectionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetMinecartFurnacePushDirectionPayload payload, ServerPlayer player) {
        if (player.containerMenu instanceof MinecartFurnaceMenu menu) {
            menu.setDirection(payload.pushDirection);
        }
    }
}
