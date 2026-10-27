package com.billstark001.hudvisibility.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityNamesMixin {
    // Remove only vanilla's F1 gate. Teams, distance, invisibility and camera checks remain.
    // EntityNamesMixin applies the per-entity policy at the common generation point.
#if MC_VERSION >= 260200
    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;isHidden()Z"))
    private boolean hudVisibility$hudGate(boolean original) { return false; }
#elif MC_VERSION >= 12111
    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;renderNames()Z"))
    private boolean hudVisibility$hudGate(boolean original) { return true; }
#else
    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;renderNames()Z"))
    private boolean hudVisibility$hudGate(boolean original) { return true; }
#endif
}
