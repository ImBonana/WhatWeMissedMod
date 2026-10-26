package me.imbanana.whatwemissed;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WhatWeMissed {
    public static final String MOD_ID = "what_we_missed";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        // Write common init code here.
    }

    public static Identifier ifOd(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
