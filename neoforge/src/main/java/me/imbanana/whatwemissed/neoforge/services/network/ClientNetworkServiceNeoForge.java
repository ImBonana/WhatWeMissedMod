package me.imbanana.whatwemissed.neoforge.services.network;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.services.network.ClientNetworkService;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID, value = Dist.CLIENT)
public class ClientNetworkServiceNeoForge implements ClientNetworkService {
    private static final List<Consumer<RegisterClientPayloadHandlersEvent>> PENDING = new ArrayList<>();

    @Override
    public <T extends CustomPacketPayload> void registerS2CHandler(CustomPacketPayload.Type<T> payload, Consumer<T> handler) {
        PENDING.add(event -> event.register(payload, (data, context) -> handler.accept(data)));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @SubscribeEvent
    public static void onRegister(RegisterClientPayloadHandlersEvent event) {
        PENDING.forEach(c -> c.accept(event));
    }
}
