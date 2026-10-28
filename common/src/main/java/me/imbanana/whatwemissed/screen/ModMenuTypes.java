package me.imbanana.whatwemissed.screen;

import me.imbanana.whatwemissed.registry.ModRegistry;
import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.screen.minecartfurnace.MinecartFurnaceMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final Supplier<MenuType<MinecartFurnaceMenu>> MINECART_FURNACE = register("minecart_furnace",MinecartFurnaceMenu::new);

    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(
            String name,
            MenuType.MenuSupplier<T> constructor
    ) {
        return ModRegistry.register(BuiltInRegistries.MENU, WhatWeMissed.ifOd(name), () -> new MenuType<>(constructor, FeatureFlagSet.of()));
    }

    public static void init() { }
}
