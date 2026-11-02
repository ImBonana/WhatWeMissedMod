package me.imbanana.whatwemissed.mixin;

import me.imbanana.whatwemissed.util.IRailBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockMixin implements IRailBlock {
    @Override
    public float getRailFrictionModifier() {
        return 1f;
    }
}
