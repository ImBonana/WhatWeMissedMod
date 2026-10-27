package me.imbanana.whatwemissed.network;

import me.imbanana.whatwemissed.services.network.NetworkService;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class ModNetwork {
    private static NetworkService service;

    public static void registerPayloads() {

    }


    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        service.sendToPlayer(player, payload);
    }

    public static void loadService(NetworkService impl) {
        ModNetwork.service = impl;
    }
}
