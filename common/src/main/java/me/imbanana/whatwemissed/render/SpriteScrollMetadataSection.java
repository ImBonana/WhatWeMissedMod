package me.imbanana.whatwemissed.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record SpriteScrollMetadataSection(float speed) {
    public static final Codec<SpriteScrollMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed").forGetter(SpriteScrollMetadataSection::speed)
    ).apply(instance, SpriteScrollMetadataSection::new));

    public static final MetadataSectionType<SpriteScrollMetadataSection> TYPE = new MetadataSectionType<>(WhatWeMissed.MOD_ID + ":scroll", CODEC);
}
