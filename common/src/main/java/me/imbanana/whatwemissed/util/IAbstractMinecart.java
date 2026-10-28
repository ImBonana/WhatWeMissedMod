package me.imbanana.whatwemissed.util;

import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManager;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public interface IAbstractMinecart {
    default void what_we_missed$link(AbstractMinecart minecart, MinecartLinkManager.ChainType chainType) { }
    default void what_we_missed$unlink() { }
    default boolean what_we_missed$hasLink() { return false; }
    default AbstractMinecart what_we_missed$getLinkedEntity() { return null; }
    default MinecartLinkManager.ChainType what_we_missed$getChainType() { return MinecartLinkManager.ChainType.IRON; }
    default boolean what_we_missed$isLinkedTo(AbstractMinecart minecart) { return false; }
}
