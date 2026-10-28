package me.imbanana.whatwemissed.event;

import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManagerClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ModClientEvents {
    public static InteractionResult onPlayerInteractionWithEntity(Player player, Level level, InteractionHand hand, Entity entity) {
        return MinecartLinkManagerClient.onClientInteract(player, level, hand, entity);
    }

    public static void onLevelChange(ClientLevel level) {
        MinecartLinkManagerClient.clearSelection();
    }

    public static void onLogout() {
        MinecartLinkManagerClient.clearSelection();
    }
}
