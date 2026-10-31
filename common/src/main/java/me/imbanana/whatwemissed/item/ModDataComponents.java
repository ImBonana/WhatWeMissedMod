package me.imbanana.whatwemissed.item;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.item.components.MinecartFurnaceFuelDataComponent;
import me.imbanana.whatwemissed.registry.ModRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final Supplier<DataComponentType<MinecartFurnaceFuelDataComponent>> MINECART_FURNACE_FUEL = register(
            "minecart_furnace_fuel",
            () -> DataComponentType.<MinecartFurnaceFuelDataComponent>builder()
                    .persistent(MinecartFurnaceFuelDataComponent.CODEC)
                    .networkSynchronized(MinecartFurnaceFuelDataComponent.STREAM_CODEC)
                    .build()
    );

    private static <T> Supplier<DataComponentType<T>> register(String name, Supplier<DataComponentType<T>> dataComponentType) {
        return ModRegistry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, WhatWeMissed.ifOd(name), dataComponentType);
    }

    public static void init() { }
}
