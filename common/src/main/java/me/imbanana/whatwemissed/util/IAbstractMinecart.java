package me.imbanana.whatwemissed.util;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public interface IAbstractMinecart {
    default void what_we_missed$link(AbstractMinecart minecart, ChainType chainType, ChainedCartLinkSide thisSide, ChainedCartLinkSide otherSide) { }
    default void what_we_missed$unlink() { }
    default boolean what_we_missed$hasLink() { return false; }
    default ChainedCartLinkSide what_we_missed$getThisLinkSide() { return ChainedCartLinkSide.BACK; }
    default ChainedCartLinkSide what_we_missed$getOtherLinkSide() { return ChainedCartLinkSide.FRONT; }
    default AbstractMinecart what_we_missed$getLinkedEntity() { return null; }
    default ChainType what_we_missed$getChainType() { return ChainType.IRON; }
    default boolean what_we_missed$isLinkedTo(AbstractMinecart minecart) { return false; }
    long what_we_missed$getLastSolveTick();
    void what_we_missed$setLastSolveTick(long tick);
}
