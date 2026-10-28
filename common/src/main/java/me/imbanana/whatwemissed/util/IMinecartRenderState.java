package me.imbanana.whatwemissed.util;

import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManager;
import net.minecraft.world.phys.Vec3;

public interface IMinecartRenderState {
    default void what_we_missed$setLinkAnchorStart(Vec3 position) { }
    default Vec3 what_we_missed$getLinkAnchorStart() {
        return Vec3.ZERO;
    }
    default void what_we_missed$setLinkAnchorEnd(Vec3 position) { }
    default Vec3 what_we_missed$getLinkAnchorEnd() {
        return Vec3.ZERO;
    }
    default void what_we_missed$setChainType(MinecartLinkManager.ChainType chainType) { };
    default MinecartLinkManager.ChainType what_we_missed$getChainType() { return MinecartLinkManager.ChainType.IRON; };
}
