package io.github.billstark001.hudvisibility.fabric;

import io.github.billstark001.hudvisibility.HudVisibility;
import net.fabricmc.api.ModInitializer;

public final class HudVisibilityFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        HudVisibility.initialize();
    }
}
