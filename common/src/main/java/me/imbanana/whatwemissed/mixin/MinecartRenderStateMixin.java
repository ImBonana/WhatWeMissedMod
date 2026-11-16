package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.util.IMinecartRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecartRenderState.class)
public abstract class MinecartRenderStateMixin implements IMinecartRenderState {
    @Unique
    private LinkRender what_we_missed$linkRender;
    @Unique
    private float what_we_missed$chestLidOpenness;

    @Override
    public void what_we_missed$setChestLidOpenness(float openness) {
        this.what_we_missed$chestLidOpenness = openness;
    }

    @Override
    public float what_we_missed$getChestLidOpenness() {
        return this.what_we_missed$chestLidOpenness;
    }

    @Override
    public void what_we_missed$setLinkRender(LinkRender link) {
        this.what_we_missed$linkRender = link;
    }

    @Override
    public LinkRender what_we_missed$getLinkRender() {
        return this.what_we_missed$linkRender;
    }
}
