package me.imbanana.whatwemissed.fabric.event;

import me.imbanana.whatwemissed.event.ModClientEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class ModClientEventsFabric {
    public static void init() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> ModClientEvents.onPlayerInteractionWithEntity(player, level, hand, entity));
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> ModClientEvents.onLevelChange(level));
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> ModClientEvents.onLogout());
    }
}
