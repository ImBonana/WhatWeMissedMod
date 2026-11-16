package me.imbanana.whatwemissed.entity;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.MinecartRenderer;

public class ModEntityRenderers {
    public static void registerRenderers() {
        EntityRenderers.register(ModEntityTypes.COPPER_CHEST_MINECART.get(), context -> new MinecartRenderer(context, ModEntityModelLayers.COPPER_CHEST_MINECART));
    }
}
