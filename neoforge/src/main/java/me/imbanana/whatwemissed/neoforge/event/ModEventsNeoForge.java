package me.imbanana.whatwemissed.neoforge.event;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.event.ModEvents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID)
public class ModEventsNeoForge {

    @SubscribeEvent
    public static void modifyItemComponents(ModifyDefaultComponentsEvent event) {
        ModEvents.modifyDefaultItemComponents((item, modifyConsumer) -> event.modifyMatching((Item testItem, DataComponentGetter ignore) -> item.test(testItem), modifyConsumer::modify));
    }
}
