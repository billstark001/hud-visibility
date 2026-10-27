package com.billstark001.hudvisibility.fabric;
import com.billstark001.hudvisibility.client.VisibilityConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
public final class HudVisibilityModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return VisibilityConfigScreen::create; }
}
