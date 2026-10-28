package me.imbanana.whatwemissed.fabric.services;

import me.imbanana.whatwemissed.services.ClientRegistryService;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.function.Supplier;

public class ClientRegistryServiceFabric implements ClientRegistryService {
    @Override
    public void registerEntityModelLayer(ModelLayerLocation layerLocation, Supplier<LayerDefinition> definitionSupplier) {
        ModelLayerRegistry.registerModelLayer(layerLocation, definitionSupplier::get);
    }
}
