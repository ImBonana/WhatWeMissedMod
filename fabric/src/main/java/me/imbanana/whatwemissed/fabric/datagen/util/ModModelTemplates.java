package me.imbanana.whatwemissed.fabric.datagen.util;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;

import java.util.Optional;

public class ModModelTemplates {
    public static final ModelTemplate RAIL_RAISED_NE_FLIPPED = createBlock("template_rail_raised_ne_flipped", "_raised_ne_flipped", TextureSlot.RAIL);
    public static final ModelTemplate RAIL_RAISED_SW_FLIPPED = createBlock("template_rail_raised_sw_flipped", "_raised_sw_flipped", TextureSlot.RAIL);
    public static final ModelTemplate CONVEYOR_RAIL_RAISED_NE_FLIPPED = createBlock("template_conveyor_rail_raised_ne_flipped", "_raised_ne_flipped", ModTextureSlots.RAIL_WOOD, ModTextureSlots.RAIL_CHAIN);
    public static final ModelTemplate CONVEYOR_RAIL_RAISED_SW_FLIPPED = createBlock("template_conveyor_rail_raised_sw_flipped", "_raised_sw_flipped", ModTextureSlots.RAIL_WOOD, ModTextureSlots.RAIL_CHAIN);
    public static final ModelTemplate CONVEYOR_RAIL_FLAT = createBlock("template_conveyor_rail_flat", ModTextureSlots.RAIL_WOOD, ModTextureSlots.RAIL_CHAIN);
    public static final ModelTemplate CONVEYOR_RAIL_RAISED_NE = createBlock("template_conveyor_rail_raised_ne", "_raised_ne", ModTextureSlots.RAIL_WOOD, ModTextureSlots.RAIL_CHAIN);
    public static final ModelTemplate CONVEYOR_RAIL_RAISED_SW = createBlock("template_conveyor_rail_raised_sw", "_raised_sw", ModTextureSlots.RAIL_WOOD, ModTextureSlots.RAIL_CHAIN);

    private static ModelTemplate createBlock(final String id, final String suffix, final TextureSlot... slots) {
        return new ModelTemplate(Optional.of(WhatWeMissed.idOf("block/" + id)), Optional.of(suffix), slots);
    }

    private static ModelTemplate createBlock(final String id, final TextureSlot... slots) {
        return new ModelTemplate(Optional.of(WhatWeMissed.idOf("block/" + id)), Optional.empty(), slots);
    }
}
