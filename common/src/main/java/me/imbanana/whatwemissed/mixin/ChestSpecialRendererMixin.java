package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.imbanana.whatwemissed.util.ChestLidRenderContext;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChestSpecialRenderer.class)
public abstract class ChestSpecialRendererMixin {
    @ModifyExpressionValue(
            method = "submit",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/special/ChestSpecialRenderer;openness:F")
    )
    private float applyMinecartLidOpenness(float original) {
        return ChestLidRenderContext.apply(original);
    }
}
