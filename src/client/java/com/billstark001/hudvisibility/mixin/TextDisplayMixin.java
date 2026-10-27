package com.billstark001.hudvisibility.mixin;

import com.billstark001.hudvisibility.Feature;
import com.billstark001.hudvisibility.client.Visibility;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancel only text body generation; the entity's optional name tag has its own policy. */
@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class TextDisplayMixin {
#if MC_VERSION >= 12111
    @Inject(method = "submitInner(Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IF)V",
        at = @At("HEAD"), cancellable = true)
#else
    @Inject(method = "renderInner(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/world/entity/Display$TextDisplay$TextRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V",
        at = @At("HEAD"), cancellable = true)
#endif
    private void hudVisibility$textBody(CallbackInfo ci) {
        if (!Visibility.visible(Feature.TEXT_DISPLAYS)) ci.cancel();
    }
}
