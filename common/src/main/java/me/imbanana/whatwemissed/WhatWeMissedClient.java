package me.imbanana.whatwemissed;

import me.imbanana.whatwemissed.network.ModClientNetwork;
import me.imbanana.whatwemissed.services.ClientPlatformBootstrap;

import java.util.ServiceLoader;

public class WhatWeMissedClient {
    public static void init() {
        ClientPlatformBootstrap services = ServiceLoader.load(ClientPlatformBootstrap.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No ClientPlatformBootstrap service file found"));

        ModClientNetwork.loadService(services.network());

        ModClientNetwork.registerClientHandlers();
    }
}
