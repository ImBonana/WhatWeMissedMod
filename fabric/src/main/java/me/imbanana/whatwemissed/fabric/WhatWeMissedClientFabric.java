package me.imbanana.whatwemissed.fabric;

import me.imbanana.whatwemissed.WhatWeMissedClient;
import me.imbanana.whatwemissed.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class WhatWeMissedClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WhatWeMissedClient.init();

        ModScreens.registerAll(MenuScreens::register);
    }
}
