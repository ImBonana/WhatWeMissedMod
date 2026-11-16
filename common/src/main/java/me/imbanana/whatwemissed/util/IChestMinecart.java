package me.imbanana.whatwemissed.util;

public interface IChestMinecart {
    default float what_we_missed$getLidOpenness(float partialTick) {
        return 0.0F;
    }
}
