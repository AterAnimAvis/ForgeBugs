package com.example.examplemod.mixin;

import com.example.examplemod.StaticState;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraftforge.fml.loading.progress.ClientVisualization")
public class ClientVisualizationMixin {

    /**
     * Error on Pre glfwInit Exceptions
     */
    @Inject(method = "initWindow()V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwInit()Z", remap = false), remap = false)
    public void beforeGLFWInit(CallbackInfo ci) {
        LogManager.getLogger().error("7285 - Handling Pre Exceptions");

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            int error = GLFW.glfwGetError(pointerbuffer);
            if (error != 0) {
                long pointerDescription = pointerbuffer.get();
                String description = pointerDescription == 0L ? "" : MemoryUtil.memUTF8(pointerDescription);
                throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", error, description));
            }
        }

        StaticState.callback = GLFW.glfwSetErrorCallback((error, description) -> StaticState.errors.add(String.format("GLFW error during init: [0x%X]%s", error, description)));
    }

    /**
     * Clear glfwInit Exceptions
     */
    @Inject(method = "initWindow()V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwDefaultWindowHints()V", remap = false), remap = false)
    public void afterGLFWInit(CallbackInfo ci) {
        LogManager.getLogger().error("7285 - Handling Post Exceptions");

        for(String error : StaticState.errors) {
            LogManager.getLogger().error("GLFW error collected during initialization: {}", error);
        }

        GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(StaticState.callback);
        if (callback != null) callback.free();
        StaticState.callback = null;
    }

}
