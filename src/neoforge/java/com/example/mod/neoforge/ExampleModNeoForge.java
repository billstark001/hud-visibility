package com.example.mod.neoforge;

import com.example.mod.ExampleMod;
import net.neoforged.fml.common.Mod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        ExampleMod.initialize();
    }
}
