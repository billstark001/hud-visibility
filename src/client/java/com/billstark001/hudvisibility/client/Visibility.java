package com.billstark001.hudvisibility.client;

import com.billstark001.hudvisibility.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;

public final class Visibility {
    private Visibility() {}
    public static boolean hudHidden() {
#if MC_VERSION >= 260200
        return Minecraft.getInstance().gui.hud.isHidden();
#else
        return Minecraft.getInstance().options.hideGui;
#endif
    }
    public static boolean visible(Feature feature) {
        return HudVisibilityClient.config().isVisible(feature, hudHidden());
    }
    public static Feature nameFeature(Entity entity) {
        if (entity instanceof Player) return Feature.PLAYER_NAMES;
        return entity instanceof Display.TextDisplay ? Feature.TEXT_DISPLAY_NAMES : Feature.ENTITY_NAMES;
    }
    public static boolean namesVisible(Entity entity) { return visible(nameFeature(entity)); }
}
