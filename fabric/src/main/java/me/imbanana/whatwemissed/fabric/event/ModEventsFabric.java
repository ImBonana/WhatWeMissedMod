package me.imbanana.whatwemissed.fabric.event;

import me.imbanana.whatwemissed.event.ModEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

public class ModEventsFabric {
    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> ModEvents.modifyDefaultItemComponents((item, modifyConsumer) -> context.modify(item, modifyConsumer::modify)));
    }
}
