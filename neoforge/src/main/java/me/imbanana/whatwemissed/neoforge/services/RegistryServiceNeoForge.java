package me.imbanana.whatwemissed.neoforge.services;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.services.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.util.Lazy;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID)
public class RegistryServiceNeoForge implements RegistryService {
    private static final List<Entry<?, ?>> ENTRIES = new ArrayList<>();
    private static final Map<ResourceKey<? extends Registry<?>>, List<Runnable>> HOOKS = new HashMap<>();

    @Override
    public <T, R extends T> Supplier<R> register(Registry<T> registry, Identifier id, Supplier<R> factory) {
        Lazy<R> lazy = createEmptyLazy();
        ENTRIES.add(new Entry<>(registry, id, factory, lazy));
        return lazy;
    }

    @Override
    public <T, R extends T> Supplier<R> register(Registry<T> registry, ResourceKey<T> key, Supplier<R> factory) {
        Lazy<R> lazy = createEmptyLazy();
        ENTRIES.add(new Entry<>(registry, key, factory, lazy));
        return lazy;
    }

    @Override
    public void whenRegistering(ResourceKey<? extends Registry<?>> registryKey, Runnable action) {
        HOOKS.computeIfAbsent(registryKey, k -> new ArrayList<>()).add(action);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        List<Runnable> hooks = HOOKS.remove(event.getRegistryKey());
        if (hooks != null) hooks.forEach(Runnable::run);

        for (Entry<?, ?> entry : List.copyOf(ENTRIES)) {
            entry.register(event);
        }
    }

    private record Entry<T, R extends T>(Registry<T> registry, @Nullable Identifier id, @Nullable ResourceKey<T> key, Supplier<R> factory, Lazy<R> holder) {
        public void register(RegisterEvent event) {
            if (id != null) {
                event.register(
                        registry.key(),
                        id,
                        () -> {
                            R instance = factory.get();
                            holder.set(instance);
                            return instance;
                        }
                );
            } else if (key != null) {
                event.register(
                        registry.key(),
                        (helper) -> {
                            helper.register(key, factory.get());
                        }
                );
            } else {
                WhatWeMissed.LOGGER.warn("[NeoForge Registry Service] entry without id or key");
            }
        }

        public Entry(Registry<T> registry, Identifier id, Supplier<R> factory, Lazy<R> holder) {
            this(registry, id, null, factory, holder);
        }

        public Entry(Registry<T> registry, ResourceKey<T> key, Supplier<R> factory, Lazy<R> holder) {
            this(registry, null, key, factory, holder);
        }
    }

    public static <T> Lazy<T> createEmptyLazy() {
        return Lazy.pure(() -> { throw new IllegalStateException("Registry entry accessed before registration"); });
    }
}
