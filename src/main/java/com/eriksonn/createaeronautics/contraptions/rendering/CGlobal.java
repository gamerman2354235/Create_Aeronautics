package com.eriksonn.createaeronautics.contraptions.rendering;


import com.eriksonn.createaeronautics.contraptions.AirshipContraptionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;

public class CGlobal {
    public static final Minecraft client = Minecraft.getInstance();
    public static AirshipContraptionRenderer renderer;
    public static ActiveRenderInfo originalCamera = client.gameRenderer.getMainCamera();
}
