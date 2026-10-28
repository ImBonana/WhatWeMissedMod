package me.imbanana.whatwemissed.network;

import me.imbanana.whatwemissed.network.payload.LinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.network.payload.UnlinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.services.network.NetworkService;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class ModNetwork {
    private static NetworkService service;

    public static void registerPayloads() {
        service.registerC2S(LinkMinecartPayloadC2S.TYPE, LinkMinecartPayloadC2S.STREAM_CODEC, LinkMinecartPayloadC2S::handleServer);
        service.registerC2S(UnlinkMinecartPayloadC2S.TYPE, UnlinkMinecartPayloadC2S.STREAM_CODEC, UnlinkMinecartPayloadC2S::handleServer);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        service.sendToPlayer(player, payload);
    }

    public static void loadService(NetworkService impl) {
        ModNetwork.service = impl;
    }
}
