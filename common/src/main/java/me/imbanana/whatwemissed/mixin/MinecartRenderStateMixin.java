package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.util.ChainType;
import me.imbanana.whatwemissed.util.IMinecartRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecartRenderState.class)
public abstract class MinecartRenderStateMixin implements IMinecartRenderState {
    @Unique
    private Vec3 what_we_missed$linkAnchorStart;
    @Unique
    private Vec3 what_we_missed$linkAnchorEnd;
    @Unique
    private ChainType what_we_missed$chainType;

    public void what_we_missed$setLinkAnchorStart(Vec3 position) {
        this.what_we_missed$linkAnchorStart = position;
    }

    public Vec3 what_we_missed$getLinkAnchorStart() {
        return what_we_missed$linkAnchorStart;
    }

    @Override
    public void what_we_missed$setLinkAnchorEnd(Vec3 position) {
        what_we_missed$linkAnchorEnd = position;
    }

    @Override
    public Vec3 what_we_missed$getLinkAnchorEnd() {
        return what_we_missed$linkAnchorEnd;
    }

    @Override
    public void what_we_missed$setChainType(ChainType chainType) {
        this.what_we_missed$chainType = chainType;
    }

    @Override
    public ChainType what_we_missed$getChainType() {
        return what_we_missed$chainType;
    }
}
