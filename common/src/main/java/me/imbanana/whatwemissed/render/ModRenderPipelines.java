package me.imbanana.whatwemissed.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.registry.ModClientRegistry;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public class ModRenderPipelines {
    public static final RenderPipeline ANIMATE_SPRITE_SCROLL = ModClientRegistry.registerRenderPipeline(
            RenderPipeline.builder(RenderPipelines.ANIMATE_SPRITE_SNIPPET)
                    .withFragmentShader(WhatWeMissed.idOf("core/animate_sprite_scroll"))
                    .withLocation(WhatWeMissed.idOf("pipeline/animate_sprite_scroll"))
                    .withBindGroupLayout(BindGroupLayouts.SPRITE)
                    .build()
    );

    public static void init() { }
}
