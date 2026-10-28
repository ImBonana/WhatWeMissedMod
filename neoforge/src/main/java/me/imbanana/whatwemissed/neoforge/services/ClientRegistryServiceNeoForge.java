package me.imbanana.whatwemissed.neoforge.services;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.services.ClientRegistryService;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID, value = Dist.CLIENT)
public class ClientRegistryServiceNeoForge implements ClientRegistryService {
    private static final List<Consumer<EntityRenderersEvent.RegisterLayerDefinitions>> PENDING = new ArrayList<>();

    @Override
    public void registerEntityModelLayer(ModelLayerLocation layerLocation, Supplier<LayerDefinition> definitionSupplier) {
        PENDING.add(registerLayerDefinitions -> registerLayerDefinitions.registerLayerDefinition(layerLocation, definitionSupplier));
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        PENDING.forEach(registerLayerDefinitionsConsumer -> registerLayerDefinitionsConsumer.accept(event));
    }
}
