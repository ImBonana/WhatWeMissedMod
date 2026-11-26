package me.imbanana.whatwemissed.util;

public interface IScrollingAnimationState {
    default void what_we_missed$setScroll(float speed, int height) { }

    default boolean what_we_missed$isScrolling() {
        return false;
    }

    default void what_we_missed$advanceScroll(float deltaTicks) { }
}
