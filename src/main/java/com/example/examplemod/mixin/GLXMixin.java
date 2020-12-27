package com.example.examplemod.mixin;

import com.example.examplemod.StaticState;
import com.mojang.blaze3d.platform.GLX;
import net.minecraft.client.MainWindow;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiConsumer;

@Mixin(GLX.class)
public class GLXMixin {

    @Redirect(method = "_initGlfw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MainWindow;checkGlfwError(Ljava/util/function/BiConsumer;)V"))
    private static void checkGlfwError(BiConsumer<Integer, String> handler) {
        if (StaticState.ENABLED && Boolean.parseBoolean(System.getProperty("fml.earlyprogresswindow", "true")))  {
            LogManager.getLogger().error("7285 - Skipped Exception Checking");
            return;
        }

        MainWindow.checkGlfwError(handler);
    }

}
