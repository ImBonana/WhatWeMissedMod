package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    @Unique
    private Entity what_we_missed$previousVehicle;

    public LocalPlayerMixin(ClientLevel level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void forgetPreviousVehicle(CallbackInfo ci) {
        this.what_we_missed$previousVehicle = null;
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void rememberPreviousVehicle(CallbackInfo ci) {
        this.what_we_missed$previousVehicle = this.getVehicle();
    }

    @WrapOperation(
            method = "startRiding",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"
            )
    )
    private SoundEngine.PlayResult playMinecartSoundOnce(SoundManager soundManager, SoundInstance sound, Operation<SoundEngine.PlayResult> original, @Local(argsOnly = true) Entity entity) {
        if (entity instanceof AbstractMinecart && entity == this.what_we_missed$previousVehicle) {
            return SoundEngine.PlayResult.NOT_STARTED;
        }

        return original.call(soundManager, sound);
    }
}
