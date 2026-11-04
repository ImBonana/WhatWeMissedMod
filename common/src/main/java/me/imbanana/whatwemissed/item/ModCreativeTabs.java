package me.imbanana.whatwemissed.item;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.block.ModBlocks;
import me.imbanana.whatwemissed.registry.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final ResourceKey<CreativeModeTab> MOD_TAB_KEY = createKey("what_we_missed");

    public static final Supplier<CreativeModeTab> MOD_TAB = register(MOD_TAB_KEY, () -> CreativeModeTab.builder(null, -1)
            .icon(() -> new ItemStack(ModBlocks.COPPER_RAIL.waxed().unaffected()))
            .title(Component.translatable("creative_tab.what_we_missed"))
            .displayItems((parameters, output) -> {
                ModBlocks.COPPER_RAIL.weathering().forEach(output::accept);
                ModBlocks.COPPER_RAIL.waxed().forEach(output::accept);
                output.accept(ModBlocks.RAIL_JUNCTION);
                output.accept(ModBlocks.DIRECTOR_RAIL);
            })
            .build()
    );

    private static ResourceKey<CreativeModeTab> createKey(final String id) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, WhatWeMissed.idOf(id));
    }

    private static Supplier<CreativeModeTab> register(ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory) {
        return ModRegistry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, factory);
    }

    public static void init() { }
}
