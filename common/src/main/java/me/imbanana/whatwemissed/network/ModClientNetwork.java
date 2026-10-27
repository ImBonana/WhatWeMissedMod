package me.imbanana.whatwemissed.network;

import me.imbanana.whatwemissed.services.network.ClientNetworkService;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ModClientNetwork {
    private static ClientNetworkService service;

    public static void registerClientHandlers() {

    }

    public static void sendToServer(CustomPacketPayload payload) {
        service.sendToServer(payload);
    }

    public static void loadService(ClientNetworkService impl) {
        ModClientNetwork.service = impl;
    }
}
