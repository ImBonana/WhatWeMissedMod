package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import me.imbanana.whatwemissed.render.SpriteScrollMetadataSection;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {
    @Shadow
    @Final
    private int width;
    @Shadow
    @Final
    private int height;

    @Shadow
    public abstract <T> Optional<T> getAdditionalMetadata(MetadataSectionType<T> type);

    @ModifyReceiver(
            method = "<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;", ordinal = 0)
    )
    private Optional<AnimationMetadataSection> animateSingleFrameScrollingSprite(Optional<AnimationMetadataSection> animationInfo, Function<?, ?> mapper, @Local(argsOnly = true) NativeImage image) {
        if (this.getAdditionalMetadata(SpriteScrollMetadataSection.TYPE).isEmpty()) return animationInfo;
        if (image.getWidth() != this.width || image.getHeight() != this.height) return animationInfo;
        if (animationInfo.isPresent() && animationInfo.get().frames().isPresent()) return animationInfo;

        AnimationMetadataSection animation = animationInfo.orElse(new AnimationMetadataSection(Optional.empty(), Optional.empty(), Optional.empty(), 1, false));
        return Optional.of(new AnimationMetadataSection(
                Optional.of(List.of(new AnimationFrame(0), new AnimationFrame(0))),
                animation.frameWidth(),
                animation.frameHeight(),
                animation.defaultFrameTime(),
                false
        ));
    }

    @ModifyReturnValue(method = "createAnimationState", at = @At("RETURN"))
    private SpriteContents.AnimationState attachScroll(SpriteContents.AnimationState state) {
        if (state != null) {
            this.getAdditionalMetadata(SpriteScrollMetadataSection.TYPE)
                    .ifPresent(scroll -> state.what_we_missed$setScroll(scroll.speed(), this.height));
        }
        return state;
    }
}
