package com.example.mod;

/** Shared initialization; keep loader and client APIs out of this class. */
public final class ExampleMod {
    public static final String MOD_ID = "example_mod";
    public static final System.Logger LOGGER = System.getLogger(MOD_ID);

    private ExampleMod() {
    }

    public static void initialize() {
        LOGGER.log(System.Logger.Level.INFO, "Initialized {0}", MOD_ID);
    }
}
