package me.imbanana.whatwemissed.render;

import me.imbanana.whatwemissed.util.IScrollingAtlas;
import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ScrollingAtlases {
    private static final Set<IScrollingAtlas> ATLASES = Collections.newSetFromMap(new WeakHashMap<>());

    private ScrollingAtlases() { }

    public static void register(IScrollingAtlas atlas) {
        ATLASES.add(atlas);
    }

    public static void unregister(IScrollingAtlas atlas) {
        ATLASES.remove(atlas);
    }

    public static void updateFrame(Minecraft minecraft) {
        if (ATLASES.isEmpty() || minecraft.isPaused()) return;
        if (minecraft.level != null && !minecraft.level.tickRateManager().runsNormally()) return;

        float deltaTicks = minecraft.getDeltaTracker().getGameTimeDeltaTicks();
        for (IScrollingAtlas atlas : ATLASES) {
            atlas.what_we_missed$updateScrollingSprites(deltaTicks);
        }
    }
}
