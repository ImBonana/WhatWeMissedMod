package me.imbanana.whatwemissed.util;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public interface IAbstractMinecart {
    default void what_we_missed$link(AbstractMinecart minecart, ChainType chainType) { }
    default void what_we_missed$unlink() { }
    default boolean what_we_missed$hasLink() { return false; }
    default AbstractMinecart what_we_missed$getLinkedEntity() { return null; }
    default ChainType what_we_missed$getChainType() { return ChainType.IRON; }
    default boolean what_we_missed$isLinkedTo(AbstractMinecart minecart) { return false; }
}
