package me.imbanana.whatwemissed.item;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.item.components.CopperStateDataComponent;
import me.imbanana.whatwemissed.item.components.MinecartFurnaceFuelDataComponent;
import me.imbanana.whatwemissed.registry.ModRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DataComponentType<MinecartFurnaceFuelDataComponent> MINECART_FURNACE_FUEL = register(
            "minecart_furnace_fuel",
            DataComponentType.<MinecartFurnaceFuelDataComponent>builder()
                    .persistent(MinecartFurnaceFuelDataComponent.CODEC)
                    .networkSynchronized(MinecartFurnaceFuelDataComponent.STREAM_CODEC)
                    .build()
    );

    public static final DataComponentType<CopperStateDataComponent> COPPER_STATE = register(
            "copper_state",
            DataComponentType.<CopperStateDataComponent>builder()
                    .persistent(CopperStateDataComponent.CODEC)
                    .networkSynchronized(CopperStateDataComponent.STREAM_CODEC)
                    .build()
    );

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> dataComponentType) {
        ModRegistry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, WhatWeMissed.idOf(name), () -> dataComponentType);
        return dataComponentType;
    }

    public static void init() { }
}
