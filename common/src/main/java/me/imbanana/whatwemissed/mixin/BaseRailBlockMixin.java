package me.imbanana.whatwemissed.mixin;

import com.mojang.datafixers.util.Pair;
import me.imbanana.whatwemissed.util.IRailBlock;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockMixin implements IRailBlock {
    @Override
    public float getRailFrictionModifier() {
        return 1f;
    }

    @Override
    public Pair<Vec3i, Vec3i> getRailExits(Vec3 deltaMovement, RailShape shape) {
        return IRailBlock.super.getRailExits(deltaMovement, shape);
    }
}
