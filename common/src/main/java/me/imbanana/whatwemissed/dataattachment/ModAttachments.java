package me.imbanana.whatwemissed.dataattachment;

import com.mojang.serialization.Codec;
import me.imbanana.whatwemissed.services.dataattachment.DataAttachmentService;
import me.imbanana.whatwemissed.services.dataattachment.EntityAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class ModAttachments {
    private static DataAttachmentService service;

    public static void loadService(DataAttachmentService service) {
        ModAttachments.service = service;
    }

    public static <T> EntityAttachment<T> entity(Identifier id, Codec<T> codec, Supplier<T> def) {
        return service.registerEntity(id, codec, null, def);
    }

    public static <T> EntityAttachment<T> syncedEntity(Identifier id, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Supplier<T> def) {
        return service.registerEntity(id, codec, streamCodec, def);
    }
}
