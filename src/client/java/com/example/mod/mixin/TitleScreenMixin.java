package com.example.mod.mixin;

import com.example.mod.ExampleMod;
import com.example.mod.client.TemplateDemo;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void exampleMod$addInfoButton(CallbackInfo callback) {
        addRenderableWidget(Button.builder(Component.literal("Template Info"),
                button -> TemplateDemo.open(this)).bounds(8, 8, 124, 20).build());
        ExampleMod.LOGGER.log(System.Logger.Level.INFO,
                "EXAMPLE_MOD_DEMO_READY compiled=" + TemplateDemo.COMPILED_MINECRAFT);
    }
}
