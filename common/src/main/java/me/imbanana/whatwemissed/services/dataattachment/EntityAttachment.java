package me.imbanana.whatwemissed.services.dataattachment;

import net.minecraft.world.entity.Entity;

public interface EntityAttachment<T> {
    T get(Entity entity);
    T getOrCreate(Entity entity);
    T set(Entity entity, T value);
    boolean has(Entity entity);
    void remove(Entity entity);
}
