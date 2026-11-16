package me.imbanana.whatwemissed.entity;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.entity.model.ModAbstractMinecartModel;
import me.imbanana.whatwemissed.registry.ModClientRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class ModEntityModelLayers {
    public static final ModelLayerLocation MINECART = createMain("minecart");
    public static final ModelLayerLocation COPPER_CHEST_MINECART = createMain("copper_chest_minecart");

    private static ModelLayerLocation createMain(String name) {
        return new ModelLayerLocation(WhatWeMissed.idOf(name), "main");
    }

    public static void registerModelLayers() {
        ModClientRegistry.registerEntityModelLayer(MINECART, ModAbstractMinecartModel::getTexturedModelData);
        ModClientRegistry.registerEntityModelLayer(COPPER_CHEST_MINECART, MinecartModel::createBodyLayer);
    }
}
