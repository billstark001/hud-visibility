package com.example.mod.fabric;

import com.example.mod.client.ExampleModClient;
import net.fabricmc.api.ClientModInitializer;

public final class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExampleModClient.initialize();
    }
}
