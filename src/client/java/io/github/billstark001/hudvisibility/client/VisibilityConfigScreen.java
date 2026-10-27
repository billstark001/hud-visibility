package io.github.billstark001.hudvisibility.client;

import io.github.billstark001.hudvisibility.Feature;
import io.github.billstark001.hudvisibility.VisibilityConfig;
import io.github.billstark001.hudvisibility.VisibilityRule;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class VisibilityConfigScreen {
    private VisibilityConfigScreen() {}
    public static Screen create(Screen parent) {
        VisibilityConfig edited = HudVisibilityClient.config().copy();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent)
            .setTitle(Component.translatable("hud_visibility.config.title"))
            .setSavingRunnable(() -> HudVisibilityClient.save(edited));
        for (boolean hidden : new boolean[] { false, true }) {
            var category = builder.getOrCreateCategory(Component.translatable(
                "hud_visibility.category." + (hidden ? "without_hud" : "with_hud")));
            for (Feature feature : Feature.values()) {
                category.addEntry(builder.entryBuilder().startBooleanToggle(
                    Component.translatable("hud_visibility.feature." + feature.key()), edited.isVisible(feature, hidden))
                    .setDefaultValue(!hidden)
                    .setTooltip(Component.translatable("hud_visibility.tooltip." + feature.key()),
                        Component.translatable("hud_visibility.tooltip.allow"))
                    .setSaveConsumer(value -> {
                        VisibilityRule old = edited.rule(feature);
                        edited.set(feature, hidden ? new VisibilityRule(old.withHud(), value) : new VisibilityRule(value, old.withoutHud()));
                    }).build());
            }
        }
        return builder.build();
    }
    public static void open(Screen parent) {
#if MC_VERSION >= 260200
        Minecraft.getInstance().gui.setScreen(create(parent));
#else
        Minecraft.getInstance().setScreen(create(parent));
#endif
    }
}
