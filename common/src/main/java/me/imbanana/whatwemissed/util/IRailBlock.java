package me.imbanana.whatwemissed.util;

public interface IRailBlock {
    default float getRailFrictionModifier() {
        return 1;
    }
}
