package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.render.ScrollingAtlases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("HEAD"))
    private void updateScrollingSprites(CallbackInfo ci) {
        ScrollingAtlases.updateFrame(this.minecraft);
    }
}
