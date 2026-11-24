package me.imbanana.whatwemissed.mixin;

import net.minecraft.world.level.block.state.properties.PistonType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PistonType.class)
enum PistonTypeMixin {
    WHAT_WE_MISSED_TACKY("what_we_missed_tacky");

    @Shadow
    PistonTypeMixin(String name) { }
}
