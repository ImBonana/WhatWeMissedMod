package me.imbanana.whatwemissed.util;

import net.minecraft.world.phys.Vec3;

public interface IMinecartRenderState {
    default void what_we_missed$setLinkRender(LinkRender link) { }
    default LinkRender what_we_missed$getLinkRender() {
        return null;
    }

    record LinkRender(Vec3 start, Vec3 end, ChainType chainType) {
    }
}
