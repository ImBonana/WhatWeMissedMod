package me.imbanana.whatwemissed.fabric;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.fabric.event.ModEventsFabric;
import net.fabricmc.api.ModInitializer;

public final class WhatWeMissedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WhatWeMissed.init();

        ModEventsFabric.init();
    }
}
