package com.billstark001.hudvisibility.mixin;

import com.billstark001.hudvisibility.client.Visibility;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
#if MC_VERSION >= 260200
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
#else
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
#endif

@Mixin(EntityRenderer.class)
public abstract class EntityNamesMixin {
#if MC_VERSION >= 260200
    // This final overload also serves subclasses with custom name distances.
    @Inject(method = "extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V",
        at = @At("HEAD"), cancellable = true)
    private void hudVisibility$names(Entity entity, EntityRenderState state, float tick, double distance, double scoreDistance, CallbackInfo ci) {
        if (!Visibility.namesVisible(entity)) {
            state.nameTag = null;
            state.scoreText = null;
            state.nameTagAttachment = null;
            ci.cancel();
        }
    }
#else
#if MC_VERSION >= 12111
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z"))
#else
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;)Z"))
#endif
    private boolean hudVisibility$names(boolean original, @Local(argsOnly = true) Entity entity) {
        return original && Visibility.namesVisible(entity);
    }
#endif
}
