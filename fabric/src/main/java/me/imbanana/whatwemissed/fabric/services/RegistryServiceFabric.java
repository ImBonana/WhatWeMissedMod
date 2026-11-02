package me.imbanana.whatwemissed.fabric.services;

import me.imbanana.whatwemissed.services.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public class RegistryServiceFabric implements RegistryService {
    @Override
    public <T, R extends T> Supplier<R> register(Registry<T> registry, Identifier id, Supplier<R> factory) {
        R instance = Registry.register(registry, id, factory.get());;
        return () -> instance;
    }

    @Override
    public <T, R extends T> Supplier<R> register(Registry<T> registry, ResourceKey<T> key, Supplier<R> factory) {
        R instance = Registry.register(registry, key, factory.get());;
        return () -> instance;
    }

    @Override
    public void whenRegistering(ResourceKey<? extends Registry<?>> registryKey, Runnable action) {
        action.run();
    }
}
