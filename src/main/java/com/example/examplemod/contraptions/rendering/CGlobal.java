package com.example.examplemod.contraptions.rendering;


import com.example.examplemod.contraptions.AirshipContraptionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;

public class CGlobal {
    public static final Minecraft client = Minecraft.getInstance();
    public static AirshipContraptionRenderer renderer;
    public static ActiveRenderInfo originalCamera = client.gameRenderer.getMainCamera();
}
