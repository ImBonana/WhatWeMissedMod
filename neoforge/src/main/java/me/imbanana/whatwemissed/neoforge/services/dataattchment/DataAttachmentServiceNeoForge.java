package me.imbanana.whatwemissed.neoforge.services.dataattchment;

import com.mojang.serialization.Codec;
import me.imbanana.whatwemissed.registry.ModRegistry;
import me.imbanana.whatwemissed.services.dataattachment.DataAttachmentService;
import me.imbanana.whatwemissed.services.dataattachment.EntityAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class DataAttachmentServiceNeoForge implements DataAttachmentService {
    @Override
    public <T> EntityAttachment<T> registerEntity(Identifier id, Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Supplier<T> defaultValue) {

        Supplier<AttachmentType<T>> attachmentType = ModRegistry.register(NeoForgeRegistries.ATTACHMENT_TYPES, id, () -> {
            AttachmentType.Builder<T> builder = AttachmentType.builder(defaultValue);
            builder.serialize(codec.fieldOf("value"));

            if (streamCodec != null) {
                builder.sync(streamCodec);
            }

            return builder.build();
        });

        return new EntityAttachment<T>() {
            @Override
            public T get(Entity entity) {
                return entity.getExistingDataOrNull(attachmentType);
            }

            @Override
            public T getOrCreate(Entity entity) {
                return entity.getData(attachmentType);
            }

            @Override
            public T set(Entity entity, T value) {
                return entity.setData(attachmentType, value);
            }

            @Override
            public boolean has(Entity entity) {
                return entity.hasData(attachmentType);
            }

            @Override
            public void remove(Entity entity) {
                entity.removeData(attachmentType);
            }
        };
    }
}
