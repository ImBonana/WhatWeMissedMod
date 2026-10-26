package me.imbanana.whatwemissed.neoforge;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.neoforged.fml.common.Mod;

@Mod(WhatWeMissed.MOD_ID)
public final class WhatWeMissedNeoForge {
    public WhatWeMissedNeoForge() {
        // Run our common setup.
        WhatWeMissed.init();
    }
}
