package com.example.mod.client;

import com.example.mod.ExampleMod;

/** Shared client entrypoint. Put client-only initialization here. */
public final class ExampleModClient {
    private ExampleModClient() {
    }

    public static void initialize() {
        ExampleMod.LOGGER.log(System.Logger.Level.DEBUG, "Initialized {0} client", ExampleMod.MOD_ID);
    }
}
