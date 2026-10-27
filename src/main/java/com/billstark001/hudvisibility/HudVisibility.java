package com.billstark001.hudvisibility;

/** Shared initialization; keep loader and client APIs out of this class. */
public final class HudVisibility {
    public static final String MOD_ID = "hud_visibility";
    public static final System.Logger LOGGER = System.getLogger(MOD_ID);

    private HudVisibility() {
    }

    public static void initialize() {
        LOGGER.log(System.Logger.Level.INFO, "Initialized {0}", MOD_ID);
    }
}
