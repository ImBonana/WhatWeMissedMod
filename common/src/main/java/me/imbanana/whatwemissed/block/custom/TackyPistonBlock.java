package me.imbanana.whatwemissed.block.custom;

import me.imbanana.whatwemissed.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.properties.PistonType;

public class TackyPistonBlock extends PistonBaseBlock {
    public TackyPistonBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    protected boolean moveBlocks(final Level level, final BlockPos pistonPos, final Direction direction, final boolean extending) {
        if (extending) {
            PistonStructureResolver resolver = new PistonStructureResolver(level, pistonPos, direction, extending);
            resolver.resolve();
            if (!resolver.getToPush().isEmpty() || !resolver.getToDestroy().isEmpty()) {
                return false;
            }
        }
        return super.moveBlocks(level, pistonPos, direction, extending);
    }

    public static PistonType getPistonType(Block block, boolean sticky) {
        if (block instanceof TackyPistonBlock) return PistonType.WHAT_WE_MISSED_TACKY;
        if (sticky) return PistonType.STICKY;
        return PistonType.DEFAULT;
    }

    public static Block getBlockFromType(PistonType pistonType) {
        return switch (pistonType) {
            case WHAT_WE_MISSED_TACKY -> ModBlocks.TACKY_PISTON;
            case STICKY -> Blocks.STICKY_PISTON;
            default -> Blocks.PISTON;
        };
    }
}
