package me.imbanana.whatwemissed.fabric;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.fabricmc.api.ModInitializer;

public final class WhatWeMissedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WhatWeMissed.init();
    }
}
