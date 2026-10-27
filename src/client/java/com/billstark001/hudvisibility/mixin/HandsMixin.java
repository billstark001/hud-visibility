package com.billstark001.hudvisibility.mixin;

import com.billstark001.hudvisibility.Feature;
import com.billstark001.hudvisibility.client.HudVisibilityClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class HandsMixin {
    // Use this frame's HUD snapshot at submission time; leave spectator/sleep/camera gates intact.
#if MC_VERSION >= 260200
    @ModifyExpressionValue(method = "renderItemInHand", at = @At(value = "FIELD",
        target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;isHudHidden:Z"))
#else
    @ModifyExpressionValue(method = "renderItemInHand", at = @At(value = "FIELD",
        target = "Lnet/minecraft/client/Options;hideGui:Z"))
#endif
    private boolean hudVisibility$hands(boolean hudHidden) {
        return !HudVisibilityClient.config().isVisible(Feature.HANDS, hudHidden);
    }
}
