package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.imbanana.whatwemissed.block.custom.TackyPistonBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PistonHeadBlock.class)
public abstract class PistonHeadBlockMixin {
    @Shadow
    @Final
    public static EnumProperty<PistonType> TYPE;

    @ModifyVariable(
            method = "isFittingBase",
            at = @At(value = "STORE"),
            ordinal = 0
    )
    private Block fixPistonTypeInIsFittingBase(Block baseBlock, @Local(ordinal = 0, argsOnly = true) BlockState armState) {
        return TackyPistonBlock.getBlockFromType(armState.getValue(TYPE));
    }

    @WrapOperation(
            method = "getCloneItemStack",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack fixPistonTypeInIsGetCloneItemStack(ItemLike item, Operation<ItemStack> original, @Local(ordinal = 0, argsOnly = true) BlockState state) {
        return original.call(TackyPistonBlock.getBlockFromType(state.getValue(TYPE)));
    }
}
