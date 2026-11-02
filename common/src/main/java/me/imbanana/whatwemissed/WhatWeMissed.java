package me.imbanana.whatwemissed;

import me.imbanana.whatwemissed.block.ModBlocks;
import me.imbanana.whatwemissed.dataattachment.ModAttachments;
import me.imbanana.whatwemissed.dataattachment.ModDataAttachmentTypes;
import me.imbanana.whatwemissed.item.ModCreativeTabs;
import me.imbanana.whatwemissed.item.ModDataComponents;
import me.imbanana.whatwemissed.network.ModNetwork;
import me.imbanana.whatwemissed.registry.ModRegistry;
import me.imbanana.whatwemissed.screen.ModMenuTypes;
import me.imbanana.whatwemissed.services.PlatformBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public final class WhatWeMissed {
    public static final String MOD_ID = "what_we_missed";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        PlatformBootstrap services = ServiceLoader.load(PlatformBootstrap.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No PlatformBootstrap service file found"));

        ModNetwork.loadService(services.network());
        ModRegistry.loadService(services.registry());
        ModAttachments.loadService(services.dataAttachment());

        ModNetwork.registerPayloads();
        ModMenuTypes.init();
        ModDataAttachmentTypes.init();
        ModDataComponents.init();
        ModRegistry.whenRegistering(Registries.BLOCK, ModBlocks::init);
        ModRegistry.whenRegistering(Registries.CREATIVE_MODE_TAB, ModCreativeTabs::init);
    }

    public static Identifier idOf(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
