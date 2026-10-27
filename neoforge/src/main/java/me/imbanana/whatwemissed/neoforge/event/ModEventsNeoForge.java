package me.imbanana.whatwemissed.neoforge.event;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.event.ModEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID)
public class ModEventsNeoForge {

    @SubscribeEvent
    public static void onEntityInteraction(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = ModEvents.onPlayerInteractionWithEntity(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget());

        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
