package com.billstark001.hudvisibility.fabric;
import com.billstark001.hudvisibility.client.HudVisibilityClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
public final class HudVisibilityFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() { HudVisibilityClient.initialize(FabricLoader.getInstance().getConfigDir()); }
}
