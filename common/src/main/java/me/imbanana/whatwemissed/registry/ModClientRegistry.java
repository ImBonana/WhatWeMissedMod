package me.imbanana.whatwemissed.registry;

import me.imbanana.whatwemissed.services.ClientRegistryService;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.function.Supplier;

public class ModClientRegistry {
    private static ClientRegistryService service;

    public static void loadService(ClientRegistryService service) {
        ModClientRegistry.service = service;
    }

    public static void registerEntityModelLayer(ModelLayerLocation layerLocation, Supplier<LayerDefinition> definitionSupplier) {
        service.registerEntityModelLayer(layerLocation, definitionSupplier);
    }
}
