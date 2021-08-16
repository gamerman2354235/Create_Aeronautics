package com.example.examplemod.mixins;

import com.example.examplemod.contraptions.rendering.CGlobal;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldRenderer.class})
public abstract class WorldRendererMixin {

    @Final
    private Minecraft minecraft;
    //@Inject(
    //        method = {"Lnet/minecraft/client/renderer/WorldRenderer;updateCameraAndRender(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V"},
    //        at = {@At(
    //                value = "INVOKE",
    //                target = "Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;finish()V",
    //                ordinal = 0,
    //                shift = At.Shift.AFTER
    //        )})
    private void onBeforeTranslucentRendering(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, ActiveRenderInfo camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {

        //CGlobal.renderer.onBeforeTranslucentRendering(matrices);
    }
}
