package io.github.billstark001.hudvisibility.mixin;

import io.github.billstark001.hudvisibility.Feature;
import io.github.billstark001.hudvisibility.client.Visibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
#if MC_VERSION >= 12111
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class HitboxMixin {
    @Inject(method = "emitGizmos", at = @At("HEAD"), cancellable = true)
    private void hudVisibility$hitboxes(CallbackInfo ci) {
        if (!Visibility.visible(Feature.HITBOXES)) ci.cancel();
    }
}
#else
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

@Mixin(EntityRenderDispatcher.class)
public abstract class HitboxMixin {
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void hudVisibility$hitboxes(CallbackInfo ci) {
        if (!Visibility.visible(Feature.HITBOXES)) ci.cancel();
    }
    @Inject(method = "renderServerSideHitbox", at = @At("HEAD"), cancellable = true)
    private static void hudVisibility$serverHitboxes(CallbackInfo ci) {
        if (!Visibility.visible(Feature.HITBOXES)) ci.cancel();
    }
}
#endif
