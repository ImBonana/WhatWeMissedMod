package me.imbanana.whatwemissed.entity;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.entity.model.ModAbstractMinecartModel;
import me.imbanana.whatwemissed.registry.ModClientRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class ModEntityModelLayers {
    public static final ModelLayerLocation MINECART = createMain("minecart");

    private static ModelLayerLocation createMain(String name) {
        return new ModelLayerLocation(WhatWeMissed.ifOd(name), "main");
    }

    public static void registerModelLayers() {
        ModClientRegistry.registerEntityModelLayer(MINECART, ModAbstractMinecartModel::getTexturedModelData);
    }
}
