package io.github.billstark001.hudvisibility.neoforge;

import io.github.billstark001.hudvisibility.HudVisibility;
import net.neoforged.fml.common.Mod;

@Mod(HudVisibility.MOD_ID)
public final class HudVisibilityNeoForge {
    public HudVisibilityNeoForge() {
        HudVisibility.initialize();
    }
}
