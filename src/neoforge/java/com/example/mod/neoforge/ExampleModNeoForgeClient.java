package com.example.mod.neoforge;

import com.example.mod.ExampleMod;
import com.example.mod.client.ExampleModClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ExampleMod.MOD_ID, dist = Dist.CLIENT)
public final class ExampleModNeoForgeClient {
    public ExampleModNeoForgeClient() {
        ExampleModClient.initialize();
    }
}
