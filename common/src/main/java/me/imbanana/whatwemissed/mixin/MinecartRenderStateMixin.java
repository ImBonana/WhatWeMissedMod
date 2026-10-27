package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import me.imbanana.whatwemissed.util.IMinecartRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecartRenderState.class)
public abstract class MinecartRenderStateMixin implements IMinecartRenderState {
    @Unique
    private Vec3 what_we_missed$linkPositionOffset;
    @Unique
    private MinecartLinkData.ChainType what_we_missed$chainType;

    @Override
    public void what_we_missed$setLinkPositionOffset(Vec3 linkPosition) {
        this.what_we_missed$linkPositionOffset = linkPosition;
    }

    @Override
    public Vec3 what_we_missed$getLinkPositionOffset() {
        return what_we_missed$linkPositionOffset;
    }

    @Override
    public void what_we_missed$setChainType(MinecartLinkData.ChainType chainType) {
        this.what_we_missed$chainType = chainType;
    }

    @Override
    public MinecartLinkData.ChainType what_we_missed$getChainType() {
        return what_we_missed$chainType;
    }
}
