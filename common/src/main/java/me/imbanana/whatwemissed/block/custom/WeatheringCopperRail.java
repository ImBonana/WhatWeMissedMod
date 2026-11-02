package me.imbanana.whatwemissed.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperRail extends CopperRailBlock implements WeatheringCopper {
    public WeatheringCopperRail(WeatherState weatherState, Properties properties) {
        super(weatherState, properties);
    }

    @Override
    public WeatherState getAge() {
        return this.weatherState;
    }

    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.changeOverTime(state, level, pos, random);
    }

    @Override
    protected boolean isRandomlyTicking(final BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

}
