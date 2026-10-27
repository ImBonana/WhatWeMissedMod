package me.imbanana.whatwemissed.services.dataattachment;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public interface DataAttachmentService {
    <T> EntityAttachment<T> registerEntity(Identifier id, Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Supplier<T> defaultValue);
}
