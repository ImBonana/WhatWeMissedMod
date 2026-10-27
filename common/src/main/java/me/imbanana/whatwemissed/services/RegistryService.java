package me.imbanana.whatwemissed.services;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public interface RegistryService {
    <T, R extends T> Supplier<R> register(Registry<T> registry, Identifier id, Supplier<R> factory);
}
