package me.imbanana.whatwemissed.services;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public interface ClientRegistryService {
    void registerEntityModelLayer(ModelLayerLocation layerLocation, Supplier<LayerDefinition> definitionSupplier);

    <E extends Entity> void registerEntityRenderer(EntityType<? extends E> entityType, EntityRendererProvider<E> rendererProvider);

    void whenRegisteringEntityRenderers(Runnable action);

    void registerRenderPipeline(RenderPipeline pipeline);
}
