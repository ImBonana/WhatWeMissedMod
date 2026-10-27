package me.imbanana.whatwemissed.fabric.services.network;

import me.imbanana.whatwemissed.services.network.ClientNetworkService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

public class ClientNetworkServiceFabric implements ClientNetworkService {
    @Override
    public <T extends CustomPacketPayload> void registerS2CHandler(CustomPacketPayload.Type<T> payload, Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(payload, (data, context) -> handler.accept(data));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
