package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.render.SpriteScrollMetadataSection;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashSet;
import java.util.Set;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {
    @ModifyVariable(method = "loadAndStitch", at = @At("HEAD"), argsOnly = true)
    private Set<MetadataSectionType<?>> addScrollMetadata(Set<MetadataSectionType<?>> additionalMetadata) {
        Set<MetadataSectionType<?>> sections = new HashSet<>(additionalMetadata);
        sections.add(SpriteScrollMetadataSection.TYPE);
        return sections;
    }
}
