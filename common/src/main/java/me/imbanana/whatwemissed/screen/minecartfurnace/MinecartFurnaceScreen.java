package me.imbanana.whatwemissed.screen.minecartfurnace;

import me.imbanana.whatwemissed.WhatWeMissed;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class MinecartFurnaceScreen extends AbstractContainerScreen<MinecartFurnaceMenu> {
    public static final Identifier CONTAINER_TEXTURE = WhatWeMissed.ifOd("textures/gui/container/minecart_furnace.png");
    public static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");

    public MinecartFurnaceScreen(MinecartFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY -= 23;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);

        if (this.menu.isLit()) {
            int litProgressHeight = Mth.ceil(this.menu.getFuelProgress() * 13.0F) + 1;
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - litProgressHeight, this.leftPos + 82, this.topPos + 35 + 14 - litProgressHeight, 14, litProgressHeight
            );
        }
    }
}
