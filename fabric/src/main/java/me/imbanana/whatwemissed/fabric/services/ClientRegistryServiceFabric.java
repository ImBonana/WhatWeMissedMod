package me.imbanana.whatwemissed.fabric.services;

import me.imbanana.whatwemissed.services.ClientRegistryService;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public class ClientRegistryServiceFabric implements ClientRegistryService {
    @Override
    public void registerEntityModelLayer(ModelLayerLocation layerLocation, Supplier<LayerDefinition> definitionSupplier) {
        ModelLayerRegistry.registerModelLayer(layerLocation, definitionSupplier::get);
    }

    @Override
    public <E extends Entity> void registerEntityRenderer(Supplier<? extends EntityType<? extends E>> entityType, EntityRendererProvider<E> rendererProvider) {
        EntityRenderers.register(entityType.get(), rendererProvider);
    }
}
