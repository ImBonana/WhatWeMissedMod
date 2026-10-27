package me.imbanana.whatwemissed.fabric.event;

import me.imbanana.whatwemissed.event.ModEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class ModEventsFabric {
    public static void init() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> ModEvents.onPlayerInteractionWithEntity(player, level, hand, entity));
    }
}
