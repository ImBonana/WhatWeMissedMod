package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.imbanana.whatwemissed.block.custom.TackyPistonBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin extends DirectionalBlock {
    @Shadow
    @Final
    private boolean isSticky;

    protected PistonBaseBlockMixin(Properties properties) {
        super(properties);
    }

    @WrapOperation(
            method = {"triggerEvent", "moveBlocks"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
            )
    )
    private <T extends Comparable<T>, V extends T> Object fixPistonType(BlockState instance, Property<T> property, V value, Operation<Object> original) {
        if (MovingPistonBlock.TYPE == property) {
            return original.call(instance, property, TackyPistonBlock.getPistonType(this, isSticky));
        }

        return original.call(instance, property, value);
    }
}
