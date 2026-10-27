package me.imbanana.whatwemissed.fabric.services.dataattchment;

import com.mojang.serialization.Codec;
import me.imbanana.whatwemissed.services.dataattachment.DataAttachmentService;
import me.imbanana.whatwemissed.services.dataattachment.EntityAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class DataAttachmentServiceFabric implements DataAttachmentService {
    @Override
    public <T> EntityAttachment<T> registerEntity(Identifier id, Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Supplier<T> defaultValue) {
        final AttachmentType<T> attachmentType = AttachmentRegistry.create(
                id,
                builder -> {
                    builder.initializer(defaultValue);
                    builder.persistent(codec);

                    if (streamCodec != null) {
                        builder.syncWith(streamCodec, AttachmentSyncPredicate.all());
                    }
                }
        );

        return new EntityAttachment<T>() {

            @Override
            public T get(Entity entity) {
                return entity.getAttached(attachmentType);
            }

            @Override
            public T getOrCreate(Entity entity) {
                return entity.getAttachedOrCreate(attachmentType);
            }

            @Override
            public T set(Entity entity, T value) {
                return entity.setAttached(attachmentType, value);
            }

            @Override
            public boolean has(Entity entity) {
                return entity.hasAttached(attachmentType);
            }

            @Override
            public void remove(Entity entity) {
                entity.removeAttached(attachmentType);
            }
        };
    }
}
