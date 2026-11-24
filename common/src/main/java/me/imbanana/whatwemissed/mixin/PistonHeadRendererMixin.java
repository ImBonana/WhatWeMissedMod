package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.imbanana.whatwemissed.block.custom.TackyPistonBlock;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PistonHeadRenderer.class)
public abstract class PistonHeadRendererMixin {
    @ModifyVariable(
            method = "extractRenderState(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private PistonType fixPistonType(PistonType pistonType, @Local(ordinal = 0) BlockState blockState) {
        return TackyPistonBlock.getPistonType(blockState.getBlock(), pistonType == PistonType.STICKY);
    }
}
