package me.imbanana.whatwemissed.registry;

import me.imbanana.whatwemissed.services.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public class ModRegistry {
    private static RegistryService service;

    public static void loadService(RegistryService service) {
        ModRegistry.service = service;
    }

    public static <T, R extends T> Supplier<R> register(Registry<T> registry, Identifier id, Supplier<R> factory) {
        return service.register(registry, id, factory);
    }

    public static <T, R extends T> Supplier<R> register(Registry<T> registry, ResourceKey<T> key, Supplier<R> factory) {
        return service.register(registry, key, factory);
    }

    public static void whenRegistering(ResourceKey<? extends Registry<?>> key, Runnable action) {
        service.whenRegistering(key, action);
    }
}
