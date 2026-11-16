package me.imbanana.whatwemissed.entity;

import me.imbanana.whatwemissed.entity.custom.MinecartCopperChestEntity;
import me.imbanana.whatwemissed.registry.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final EntityType<MinecartCopperChestEntity> COPPER_CHEST_MINECART = register(
            ModEntityTypeIds.COPPER_CHEST_MINECART,
            EntityType.Builder.of(MinecartCopperChestEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
    );

    private static <T extends Entity> EntityType<T> register(ResourceKey<EntityType<?>> key, EntityType.Builder<T> builder) {
        EntityType<T> entityType = builder.build(key);
        ModRegistry.register(BuiltInRegistries.ENTITY_TYPE, key, () -> entityType);
        return entityType;
    }

    public static void init() { }
}
