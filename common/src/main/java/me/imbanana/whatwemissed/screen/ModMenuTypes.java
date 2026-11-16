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
    public static final MenuType<MinecartFurnaceMenu> MINECART_FURNACE = register("minecart_furnace", MinecartFurnaceMenu::new);

    public static <T extends AbstractContainerMenu> MenuType<T> register(
            String name,
            MenuType.MenuSupplier<T> constructor
    ) {
        MenuType<T> menuType = new MenuType<>(constructor, FeatureFlagSet.of());
        ModRegistry.register(BuiltInRegistries.MENU, WhatWeMissed.idOf(name), () -> menuType);
        return menuType;
    }

    public static void init() { }
}
