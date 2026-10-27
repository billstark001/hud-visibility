package com.billstark001.hudvisibility.mixin;

import com.billstark001.hudvisibility.client.VisibilityConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** An always available entry point, including Fabric installations without Mod Menu. */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) { super(title); }
    @Inject(method = "init", at = @At("TAIL"))
    private void hudVisibility$addConfig(CallbackInfo ci) {
        addRenderableWidget(Button.builder(Component.translatable("hud_visibility.config.button"),
            button -> VisibilityConfigScreen.open(this)).bounds(8, 8, 110, 20).build());
    }
}
