package com.example.mod.client;

import com.example.mod.ExampleMod;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A small working example of compiling one feature against different game APIs. */
public final class TemplateDemo {
#if MC_VERSION == 260300
    public static final String COMPILED_MINECRAFT = "26.3";
    public static final int PREPROCESSOR_VERSION = 260300;
#elif MC_VERSION == 260200
    public static final String COMPILED_MINECRAFT = "26.2";
    public static final int PREPROCESSOR_VERSION = 260200;
#elif MC_VERSION == 12111
    public static final String COMPILED_MINECRAFT = "1.21.11";
    public static final int PREPROCESSOR_VERSION = 12111;
#elif MC_VERSION == 12101
    public static final String COMPILED_MINECRAFT = "1.21.1";
    public static final int PREPROCESSOR_VERSION = 12101;
#else
#error "Unsupported MC_VERSION: add a template demo branch for this target."
#endif

    private TemplateDemo() {
    }

    public static String runtimeVersion() {
#if MC_VERSION >= 12111
        return SharedConstants.getCurrentVersion().name();
#else
        return SharedConstants.getCurrentVersion().getName();
#endif
    }

    public static void open(Screen parent) {
        String runtime = runtimeVersion();
        boolean matches = COMPILED_MINECRAFT.equals(runtime);
#if MC_VERSION >= 260200
        String screenApi = "Gui.setScreen";
#else
        String screenApi = "Minecraft.setScreen";
#endif
        String message = "Compiled: " + COMPILED_MINECRAFT + " | Runtime: " + runtime
                + "\nMC_VERSION = " + PREPROCESSOR_VERSION
                + "\nScreen API: " + screenApi
                + "\nVersion check: " + (matches ? "PASS" : "FAIL");
        setScreen(new AlertScreen(() -> {
            setScreen(parent);
            ExampleMod.LOGGER.log(System.Logger.Level.INFO,
                    "EXAMPLE_MOD_DEMO_CLOSE compiled=" + COMPILED_MINECRAFT);
        }, Component.literal("Template Info"), Component.literal(message)));
        ExampleMod.LOGGER.log(System.Logger.Level.INFO,
                "EXAMPLE_MOD_DEMO_OPEN compiled=" + COMPILED_MINECRAFT + " runtime=" + runtime
                        + " macro=" + PREPROCESSOR_VERSION + " api=" + screenApi + " matches=" + matches);
    }

    private static void setScreen(Screen screen) {
#if MC_VERSION >= 260200
        Minecraft.getInstance().gui.setScreen(screen);
#else
        Minecraft.getInstance().setScreen(screen);
#endif
    }
}
