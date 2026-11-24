package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.imbanana.whatwemissed.block.custom.TackyPistonBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin {
    @Shadow
    private BlockState movedState;

    @WrapOperation(
            method = "getCollisionRelatedBlockState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;",
                    ordinal = 1
            )
    )
    private <T extends Comparable<T>, V extends T> Object fixPistonType(BlockState instance, Property<T> property, V value, Operation<Object> original) {
        return original.call(instance, property, TackyPistonBlock.getPistonType(this.movedState.getBlock(), this.movedState.is(Blocks.STICKY_PISTON)));
    }
}
