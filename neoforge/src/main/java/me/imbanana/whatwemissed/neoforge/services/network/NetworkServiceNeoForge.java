package me.imbanana.whatwemissed.neoforge.services.network;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.services.network.NetworkService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID)
public class NetworkServiceNeoForge implements NetworkService {
    private static final List<Consumer<PayloadRegistrar>> PENDING = new ArrayList<>();

    @Override
    public <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> payload, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler) {
        PENDING.add(payloadRegistrar -> payloadRegistrar.playToServer(payload, codec, (data, context) -> handler.accept(data, (ServerPlayer) context.player())));
    }

    @Override
    public <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> payload, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PENDING.add(payloadRegistrar -> payloadRegistrar.playToClient(payload, codec));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @SubscribeEvent
    public static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        PENDING.forEach(c -> c.accept(registrar));
    }
}
