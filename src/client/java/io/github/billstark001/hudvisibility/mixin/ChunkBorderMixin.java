package io.github.billstark001.hudvisibility.mixin;

import io.github.billstark001.hudvisibility.Feature;
import io.github.billstark001.hudvisibility.client.Visibility;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkBorderRenderer.class)
public abstract class ChunkBorderMixin {
#if MC_VERSION >= 12111
    @Inject(method = "emitGizmos", at = @At("HEAD"), cancellable = true)
#else
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
#endif
    private void hudVisibility$chunkBorders(CallbackInfo ci) {
        if (!Visibility.visible(Feature.CHUNK_BORDERS)) ci.cancel();
    }
}
