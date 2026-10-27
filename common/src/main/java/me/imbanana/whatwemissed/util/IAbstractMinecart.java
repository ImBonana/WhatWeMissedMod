package me.imbanana.whatwemissed.util;

import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

public interface IAbstractMinecart {
    default void what_we_missed$link(AbstractMinecart minecart, MinecartLinkData.ChainType chainType) { }
    default void what_we_missed$unlink() { }
    default boolean what_we_missed$hasLink() { return false; }
    default AbstractMinecart what_we_missed$getLinkedEntity() { return null; }
    default MinecartLinkData.ChainType what_we_missed$getChainType() { return MinecartLinkData.ChainType.IRON; }
    default boolean what_we_missed$isLinkedTo(AbstractMinecart minecart) { return false; };
    default Vec3 what_we_missed$getLinkPosition(float partialTick) {
        return Vec3.ZERO;
    }
}
