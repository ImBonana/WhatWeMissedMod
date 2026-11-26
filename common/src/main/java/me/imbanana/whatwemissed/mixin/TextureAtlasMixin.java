package me.imbanana.whatwemissed.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import me.imbanana.whatwemissed.render.ScrollingAtlases;
import me.imbanana.whatwemissed.util.IScrollingAtlas;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin implements IScrollingAtlas {
    @Shadow
    private List<SpriteContents.AnimationState> animatedTexturesStates;
    @Shadow
    private int maxMipLevel;
    @Shadow
    private GpuTextureView[] mipViews;
    @Shadow
    @Final
    private Identifier location;

    @Unique
    private List<SpriteContents.AnimationState> what_we_missed$scrollingStates = List.of();

    @Inject(method = "upload", at = @At("TAIL"))
    private void collectScrollingStates(CallbackInfo ci) {
        this.what_we_missed$scrollingStates = this.animatedTexturesStates.stream()
                .filter(SpriteContents.AnimationState::what_we_missed$isScrolling)
                .toList();

        if (this.what_we_missed$scrollingStates.isEmpty()) {
            ScrollingAtlases.unregister(this);
        } else {
            ScrollingAtlases.register(this);
        }
    }

    @Inject(method = "clearTextureData", at = @At("HEAD"))
    private void clearScrollingStates(CallbackInfo ci) {
        this.what_we_missed$scrollingStates = List.of();
        ScrollingAtlases.unregister(this);
    }

    @Override
    public void what_we_missed$updateScrollingSprites(float deltaTicks) {
        if (this.what_we_missed$scrollingStates.isEmpty()) return;

        for (SpriteContents.AnimationState state : this.what_we_missed$scrollingStates) {
            state.what_we_missed$advanceScroll(deltaTicks);
        }

        for (int level = 0; level <= this.maxMipLevel; level++) {
            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Scroll " + this.location, this.mipViews[level], Optional.empty())) {
                RenderSystem.bindDefaultUniforms(renderPass);

                for (SpriteContents.AnimationState state : this.what_we_missed$scrollingStates) {
                    state.drawToAtlas(renderPass, state.getDrawUbo(level));
                }
            }
        }
    }
}
