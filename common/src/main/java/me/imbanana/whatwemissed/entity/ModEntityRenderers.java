package me.imbanana.whatwemissed.entity;

import me.imbanana.whatwemissed.registry.ModClientRegistry;
import net.minecraft.client.renderer.entity.MinecartRenderer;

public class ModEntityRenderers {
    public static void registerRenderers() {
        ModClientRegistry.registerEntityRenderer(ModEntityTypes.COPPER_CHEST_MINECART, context -> new MinecartRenderer(context, ModEntityModelLayers.COPPER_CHEST_MINECART));
    }
}
