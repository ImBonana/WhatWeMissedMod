package me.imbanana.whatwemissed.fabric;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.block.ModBlocks;
import me.imbanana.whatwemissed.fabric.event.ModEventsFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;

public final class WhatWeMissedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WhatWeMissed.init();
        ModEventsFabric.init();

        OxidizableBlocksRegistry.registerWeatheringCopperBlocks(ModBlocks.COPPER_RAIL);
    }
}
