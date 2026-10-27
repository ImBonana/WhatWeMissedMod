package me.imbanana.whatwemissed.services.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

public interface ClientNetworkService {
    <T extends CustomPacketPayload> void registerS2CHandler(CustomPacketPayload.Type<T> payload, Consumer<T> handler);
    void sendToServer(CustomPacketPayload payload);
}
