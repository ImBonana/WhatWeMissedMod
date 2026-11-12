package me.imbanana.whatwemissed.util;

import me.imbanana.whatwemissed.managers.minecart.train.TrainState;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public interface IAbstractMinecart {
    default AbstractMinecart what_we_missed$getParent() { return null; }
    default AbstractMinecart what_we_missed$getChild() { return null; }
    default ChainedCartLinkSide what_we_missed$getParentSide() { return ChainedCartLinkSide.FRONT; }
    default ChainedCartLinkSide what_we_missed$getChildSide() { return ChainedCartLinkSide.BACK; }
    default ChainType what_we_missed$getParentChainType() { return ChainType.IRON; }
    default boolean what_we_missed$linkChild(ChainedCartLinkSide thisSide, AbstractMinecart child, ChainedCartLinkSide childSide, ChainType chainType) { return false; }
    default void what_we_missed$unlinkParent() { }
    default void what_we_missed$unlinkChild() { }
    default void what_we_missed$turnTrainAround() { }

    default boolean what_we_missed$hasLink() {
        return this.what_we_missed$getParent() != null || this.what_we_missed$getChild() != null;
    }

    default boolean what_we_missed$hasParentOn(ChainedCartLinkSide side) {
        return this.what_we_missed$getParentSide() == side && this.what_we_missed$getParent() != null;
    }

    default boolean what_we_missed$hasChildOn(ChainedCartLinkSide side) {
        return this.what_we_missed$getChildSide() == side && this.what_we_missed$getChild() != null;
    }

    default boolean what_we_missed$hasLinkOn(ChainedCartLinkSide side) {
        return this.what_we_missed$hasParentOn(side) || this.what_we_missed$hasChildOn(side);
    }

    default boolean what_we_missed$isLinkedTo(AbstractMinecart minecart) {
        return minecart != null && (minecart == this.what_we_missed$getParent() || minecart == this.what_we_missed$getChild());
    }

    default void what_we_missed$unlink(ChainedCartLinkSide side) {
        if (this.what_we_missed$hasParentOn(side)) this.what_we_missed$unlinkParent();
        else if (this.what_we_missed$hasChildOn(side)) this.what_we_missed$unlinkChild();
    }

    default long what_we_missed$getTrainTick() { return -1; }
    default void what_we_missed$setTrainTick(long tick) { }
    default TrainState what_we_missed$getTrainState() { return null; }
    default void what_we_missed$setTrainState(TrainState state) { }
}
