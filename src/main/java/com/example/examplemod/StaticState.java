package com.example.examplemod;

import com.google.common.collect.Lists;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.List;

public class StaticState {

    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("forge.7285.extra", "false"));

    public static final List<String> errors = Lists.newArrayList();
    public static GLFWErrorCallback callback = null;

}
