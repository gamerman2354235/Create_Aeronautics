package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipContraption;
import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderInfo;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value= ContraptionRenderInfo.class)
public class ContraptionRenderInfoMixin {


    @Inject(locals = LocalCapture.CAPTURE_FAILHARD,remap=false,method = "setupMatrices", at = @At(remap=false,value = "INVOKE", target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V"))
    public void onSetupMatrices(MatrixStack viewProjection, double camX, double camY, double camZ, CallbackInfo ci, AbstractContraptionEntity entity, double x, double y, double z)
    {
        if(entity instanceof ControlledContraptionEntity && entity.level.dimension() == AirshipDimensionManager.WORLD_ID) {
            int plotId = AirshipManager.getIdFromPlotPos(entity.blockPosition());
            AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllAirships.get(plotId);
            BlockPos anchorPos = AirshipManager.getPlotPosFromId(plotId);
            //viewProjection.popPose();
            //viewProjection.pushPose();
            //Vector3d v = airshipEntity .position().add(entity.position()).subtract(new Vector3d(anchorPos.getX(),anchorPos.getY(),anchorPos.getZ()));
            //viewProjection.translate(v.x-camX,v.y-camY,v.z-camZ);
            //x=v.x-camX;

        }
    }

    //@Mutable
    //@Final
    //@Shadow
    //public final Contraption contraption;
    //@Mutable
    //@Final
    //@Shadow
    //private final ContraptionMatrices matrices;
    //public ContraptionRenderInfoMixin(Contraption contraption,ContraptionMatrices matrices) {
    //    this.contraption = contraption;
    //    this.matrices=matrices;
    //}
    @Mutable
    @Shadow
    private boolean visible;
    ///**
    // * @author Eriksonn
    // */
    //@Overwrite(remap = false)
    //public void beginFrame(BeginFrameEvent event) {
    //    matrices.clear();
//
    //    AbstractContraptionEntity entity = contraption.entity;
    //    if(entity instanceof ControlledContraptionEntity && entity.level.dimension() == AirshipDimensionManager.WORLD_ID) {
    //        int plotId = AirshipManager.getIdFromPlotPos(entity.blockPosition());
    //        AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllAirships.get(plotId);
    //        if (airshipEntity != null) {
    //            entity = airshipEntity;
    //        }
    //    }
    //    visible = event.getClippingHelper().isVisible(entity.getBoundingBoxForCulling().inflate(2));
    //}
    @Inject(locals = LocalCapture.CAPTURE_FAILHARD,remap=false,method = "beginFrame", at = @At("TAIL"))
    public void afterBeginFrame(BeginFrameEvent event,CallbackInfo ci,AbstractContraptionEntity entity) {
        if (entity instanceof ControlledContraptionEntity && entity.level.dimension() == AirshipDimensionManager.WORLD_ID) {
            int plotId = AirshipManager.getIdFromPlotPos(entity.blockPosition());
            AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllAirships.get(plotId);
            if (airshipEntity != null) {
                visible = event.getClippingHelper().isVisible(airshipEntity.getBoundingBoxForCulling().inflate(2));
            }
        }
//
    }

}
