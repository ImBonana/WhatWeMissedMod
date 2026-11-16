package me.imbanana.whatwemissed.neoforge.services;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.services.ClientRegistryService;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
    private static final List<Consumer<EntityRenderersEvent.RegisterRenderers>> PENDING_RENDERERS = new ArrayList<>();

    @Override
    public void registerEntityModelLayer(ModelLayerLocation layerLocation, Supplier<LayerDefinition> definitionSupplier) {
        PENDING.add(registerLayerDefinitions -> registerLayerDefinitions.registerLayerDefinition(layerLocation, definitionSupplier));
    }

    @Override
    public <E extends Entity> void registerEntityRenderer(Supplier<? extends EntityType<? extends E>> entityType, EntityRendererProvider<E> rendererProvider) {
        PENDING_RENDERERS.add(registerRenderers -> registerRenderers.registerEntityRenderer(entityType.get(), rendererProvider));
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        PENDING.forEach(registerLayerDefinitionsConsumer -> registerLayerDefinitionsConsumer.accept(event));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        PENDING_RENDERERS.forEach(registerRenderersConsumer -> registerRenderersConsumer.accept(event));
    }
}
