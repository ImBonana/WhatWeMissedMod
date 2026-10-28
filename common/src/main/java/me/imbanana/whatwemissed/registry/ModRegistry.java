package me.imbanana.whatwemissed.registry;

import me.imbanana.whatwemissed.services.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class ModRegistry {
    private static RegistryService service;

    public static void loadService(RegistryService service) {
        ModRegistry.service = service;
    }

    public static <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> factory) {
        return service.register(registry, id, factory);
    }
}
