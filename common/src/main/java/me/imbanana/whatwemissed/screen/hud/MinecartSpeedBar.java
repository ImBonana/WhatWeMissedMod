package me.imbanana.whatwemissed.screen.hud;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class MinecartSpeedBar implements ContextualBar {
    private static final Identifier SPEED_BAR_BACKGROUND_SPRITE = WhatWeMissed.idOf("hud/speed_bar_background");
    private static final Identifier SPEED_BAR_PROGRESS_SPRITE = WhatWeMissed.idOf("hud/speed_bar_progress");
    private final Minecraft minecraft;
    private final AbstractMinecart minecart;

    public MinecartSpeedBar(final Minecraft minecraft) {
        this.minecraft = minecraft;
        if (minecraft.player.getVehicle() instanceof AbstractMinecart cart) {
            this.minecart = cart;
        } else {
            throw new RuntimeException("Cannot display minecart speed bar, minecart cannot be found");
        }
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker) {
        int left = this.left(this.minecraft.getWindow());
        int top = this.top(this.minecraft.getWindow());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPEED_BAR_BACKGROUND_SPRITE, left, top, 182, 5);
        int progress = Mth.lerpDiscrete((float) (minecart.getDeltaMovement().length() / minecart.getMaxSpeed(null)), 0, 182);
        if (progress > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPEED_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, left, top, progress, 5);
        }
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {

    }
}
