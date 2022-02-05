package com.eriksonn.createaeronautics.contraptions;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class AirshipContraptionEntityRenderer extends EntityRenderer<AirshipContraptionEntity> {
    public AirshipContraptionEntityRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(AirshipContraptionEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(AirshipContraptionEntity entity, ClippingHelper clippingHelper, double cameraX, double cameraY,
                                double cameraZ) {
        if (entity.getContraption() == null)
            return false;
        if (!entity.isAlive())
            return false;
        //return false;
        return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
    }

    @Override
    public void render(AirshipContraptionEntity entity, float yaw, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
                       int overlay) {
        super.render(entity, yaw, partialTicks, ms, buffers, overlay);
        Contraption contraption = entity.getContraption();
        BlockPos anchorPos = AirshipManager.getPlotPosFromId(entity.plotId);


        //ms.translate(-anchorPos.getX(),-anchorPos.getY(),-anchorPos.getZ());//does not help

        if (contraption != null) {
            //ContraptionRenderDispatcher.renderFromEntity(entity, contraption, buffers);
        }

    }
}
