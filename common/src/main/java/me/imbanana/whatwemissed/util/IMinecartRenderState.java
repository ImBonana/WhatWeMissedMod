package me.imbanana.whatwemissed.util;

import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import net.minecraft.world.phys.Vec3;

public interface IMinecartRenderState {
    default void what_we_missed$setLinkPositionOffset(Vec3 position) { }
    default Vec3 what_we_missed$getLinkPositionOffset() {
        return Vec3.ZERO;
    }
    default void what_we_missed$setChainType(MinecartLinkData.ChainType chainType) { };
    default MinecartLinkData.ChainType what_we_missed$getChainType() { return MinecartLinkData.ChainType.IRON; };
}
