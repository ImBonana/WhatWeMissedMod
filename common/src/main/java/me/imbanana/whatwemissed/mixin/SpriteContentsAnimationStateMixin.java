package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import me.imbanana.whatwemissed.render.ModRenderPipelines;
import me.imbanana.whatwemissed.util.IScrollingAnimationState;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SpriteContents.AnimationState.class)
public abstract class SpriteContentsAnimationStateMixin implements IScrollingAnimationState {
    @Unique
    private float what_we_missed$scrollSpeed;
    @Unique
    private int what_we_missed$scrollHeight;
    @Unique
    private float what_we_missed$scrollOffset;

    @Override
    public void what_we_missed$setScroll(float speed, int height) {
        this.what_we_missed$scrollSpeed = speed;
        this.what_we_missed$scrollHeight = height;
        this.what_we_missed$scrollOffset = 0.0F;
    }

    @Override
    public boolean what_we_missed$isScrolling() {
        return this.what_we_missed$scrollHeight > 0;
    }

    @Override
    public void what_we_missed$advanceScroll(float deltaTicks) {
        if (!this.what_we_missed$isScrolling()) return;

        this.what_we_missed$scrollOffset = Mth.positiveModulo(this.what_we_missed$scrollOffset + this.what_we_missed$scrollSpeed * deltaTicks, this.what_we_missed$scrollHeight);
    }

    @ModifyExpressionValue(
            method = "drawToAtlas",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/texture/SpriteContents$AnimatedTexture;interpolateFrames:Z")
    )
    private boolean skipInterpolationWhenScrolling(boolean original) {
        return original && !this.what_we_missed$isScrolling();
    }

    @ModifyExpressionValue(
            method = "drawToAtlas",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;isDirty:Z")
    )
    private boolean alwaysBlitWhenScrolling(boolean original) {
        return original || this.what_we_missed$isScrolling();
    }

    @ModifyArg(
            method = "drawToAtlas",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getCompiledPipeline(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;")
    )
    private RenderPipeline useScrollPipeline(RenderPipeline pipeline) {
        return this.what_we_missed$isScrolling() ? ModRenderPipelines.ANIMATE_SPRITE_SCROLL : pipeline;
    }

    @ModifyArg(
            method = "drawToAtlas",
            at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/RenderPass;draw(IIII)V"),
            index = 2
    )
    private int encodeScrollProgress(int firstVertex) {
        if (!this.what_we_missed$isScrolling()) return firstVertex;

        int progress = (int) (this.what_we_missed$scrollOffset / this.what_we_missed$scrollHeight * 1000.0F);
        return progress << 3;
    }
}
