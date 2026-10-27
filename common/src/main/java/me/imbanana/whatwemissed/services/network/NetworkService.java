package me.imbanana.whatwemissed.services.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public interface NetworkService {
    <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> payload, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler);
    <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> payload, StreamCodec<? super RegistryFriendlyByteBuf, T> codec);
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
