package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.imbanana.whatwemissed.screen.hud.MinecartSpeedBar;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private MinecartSpeedBar what_we_missed$speedBar;
    @Unique
    private AbstractMinecart what_we_missed$speedBarCart;

    @Unique
    private ContextualBar what_we_missed$minecartBar() {
        if (this.minecraft.player != null && this.minecraft.player.getVehicle() instanceof AbstractMinecart cart) {
            if (this.what_we_missed$speedBar == null || this.what_we_missed$speedBarCart != cart) {
                this.what_we_missed$speedBar = new MinecartSpeedBar(this.minecraft);
                this.what_we_missed$speedBarCart = cart;
            }
            return this.what_we_missed$speedBar;
        }

        this.what_we_missed$speedBar = null;
        this.what_we_missed$speedBarCart = null;
        return null;
    }

    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
            )
    )
    private void drawSpeedBarBackground(ContextualBar instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ContextualBar speedBar = this.what_we_missed$minecartBar();
        original.call(speedBar != null ? speedBar : instance, graphics, deltaTracker);
    }

    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
            )
    )
    private void drawSpeedBarForeground(ContextualBar instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ContextualBar speedBar = this.what_we_missed$minecartBar();
        original.call(speedBar != null ? speedBar : instance, graphics, deltaTracker);
    }
}
