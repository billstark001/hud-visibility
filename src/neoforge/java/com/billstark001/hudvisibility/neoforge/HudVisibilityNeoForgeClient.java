package com.billstark001.hudvisibility.neoforge;
import com.billstark001.hudvisibility.HudVisibility;
import com.billstark001.hudvisibility.client.HudVisibilityClient;
import com.billstark001.hudvisibility.client.VisibilityConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
@Mod(value = HudVisibility.MOD_ID, dist = Dist.CLIENT)
public final class HudVisibilityNeoForgeClient {
    public HudVisibilityNeoForgeClient(ModContainer container) {
        HudVisibilityClient.initialize(FMLPaths.CONFIGDIR.get());
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> VisibilityConfigScreen.create(parent));
    }
}
