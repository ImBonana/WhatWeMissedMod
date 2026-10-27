package me.imbanana.whatwemissed;

import me.imbanana.whatwemissed.network.ModNetwork;
import me.imbanana.whatwemissed.screen.ModMenuTypes;
import me.imbanana.whatwemissed.services.PlatformBootstrap;
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

        ModNetwork.registerPayloads();
        ModMenuTypes.init();
    }

    public static Identifier ifOd(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
