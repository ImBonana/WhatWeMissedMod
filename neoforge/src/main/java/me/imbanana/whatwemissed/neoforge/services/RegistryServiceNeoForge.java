package me.imbanana.whatwemissed.neoforge.services;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.services.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(modid = WhatWeMissed.MOD_ID)
public class RegistryServiceNeoForge implements RegistryService {
    private static final List<Entry<?, ?>> ENTRIES = new ArrayList<>();

    @Override
    public <T, R extends T> Supplier<R> register(Registry<T> registry, Identifier id, Supplier<R> factory) {
        Lazy<R> lazy = Lazy.pure(() -> { throw new IllegalStateException("Registry entry accessed before registration"); });
        ENTRIES.add(new Entry<>(registry, id, factory, lazy));
        return lazy;
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        ENTRIES.forEach(entry -> entry.register(event));
    }

    private record Entry<T, R extends T>(Registry<T> registry, Identifier id, Supplier<R> factory, Lazy<R> holder) {
        public void register(RegisterEvent event) {
            event.register(
                    registry.key(),
                    id,
                    () -> {
                        R instance = factory.get();
                        holder.set(instance);
                        return instance;
                    }
            );
        }
    }
}
