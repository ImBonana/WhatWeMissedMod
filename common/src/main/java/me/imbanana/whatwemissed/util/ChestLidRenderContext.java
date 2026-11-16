package me.imbanana.whatwemissed.util;

public final class ChestLidRenderContext {
    private static float openness = 0.0F;

    private ChestLidRenderContext() { }

    public static void setOpenness(float openness) {
        ChestLidRenderContext.openness = openness;
    }

    public static float apply(float original) {
        return openness > 0.0F ? openness : original;
    }
}
