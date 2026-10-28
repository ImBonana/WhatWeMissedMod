package me.imbanana.whatwemissed.neoforge.event;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.event.ModClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID, value = Dist.CLIENT)
public class ModEventsNeoForge {

    @SubscribeEvent
    public static void onEntityInteraction(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = ModClientEvents.onPlayerInteractionWithEntity(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget());

        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientLevelChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        ModClientEvents.onLevelChange(Minecraft.getInstance().level);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ModClientEvents.onLogout();
    }
}
