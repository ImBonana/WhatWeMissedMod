package me.imbanana.whatwemissed.neoforge;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.WhatWeMissedClient;
import me.imbanana.whatwemissed.screen.ModScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = WhatWeMissed.MOD_ID, dist = Dist.CLIENT)
public class WhatWeMissedClientNeoForge {
    public WhatWeMissedClientNeoForge(IEventBus modBus) {
        WhatWeMissedClient.init();

        modBus.addListener(this::registerScreens);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        ModScreens.registerAll(event::register);
    }
}
