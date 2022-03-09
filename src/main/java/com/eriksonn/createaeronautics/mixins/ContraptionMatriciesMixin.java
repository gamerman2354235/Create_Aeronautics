package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.utils.AbstractContraptionEntityExtension;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value= ContraptionMatrices.class)
public class ContraptionMatriciesMixin {

    @Shadow MatrixStack model;

    @Inject(locals = LocalCapture.CAPTURE_FAILHARD,remap=false,method = "setup", at = @At(remap=false,value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/components/structureMovement/AbstractContraptionEntity;doLocalTransforms(F[Lcom/mojang/blaze3d/matrix/MatrixStack;)V"))
    private void onSetupTransforms(MatrixStack viewProjection, AbstractContraptionEntity entity, CallbackInfo ci)
    {
        if(entity instanceof ControlledContraptionEntity && entity.level.dimension() == AirshipDimensionManager.WORLD_ID)
        {
            int plotId = AirshipManager.getIdFromPlotPos(entity.blockPosition());
            AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllAirships.get(plotId);
            if(airshipEntity!=null) {
                BlockPos anchor = AirshipManager.getPlotPosFromId(plotId);
                Vector3d relativePos = entity.position().subtract(new Vector3d(anchor.getX(),anchor.getY(),anchor.getZ()));

                double x = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), airshipEntity.xOld, airshipEntity.getX());
                double y = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), airshipEntity.yOld, airshipEntity.getY());
                double z = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), airshipEntity.zOld, airshipEntity.getZ());
                Vector3d globalPos=new Vector3d(x,y,z).subtract(entity.position());
                //Vector3d globalPos=airshipEntity.position().subtract(entity.position());
                //globalPos=globalPos.add(airshipEntity.velocity.scale(AnimationTickHolder.getPartialTicks()));
                model.translate(globalPos.x,globalPos.y,globalPos.z);
                model.translate(5.5,0.5,0.5);
                Quaternion Q = airshipEntity.quat.copy();
                Q.conj();
                model.mulPose(Q);
                model.translate(-0.5,-0.5,-0.5);
                model.translate(relativePos.x,relativePos.y,relativePos.z);
                Vector3d newPos = ((AbstractContraptionEntityExtension)entity).createAeronautics$getOriginalPosition();
                //entity.setPos(newPos.x,newPos.y,newPos.z);
            }
        }
    }
    //@Inject(locals = LocalCapture.CAPTURE_FAILHARD,remap=false,method = "setup", at = @At("HEAD"))
    //public void onSetupStart(MatrixStack viewProjection, AbstractContraptionEntity entity, CallbackInfo ci)
    //{
    //    if(entity instanceof ControlledContraptionEntity && entity.level.dimension() == AirshipDimensionManager.WORLD_ID)
    //    {
    //        int plotId = AirshipManager.getIdFromPlotPos(entity.blockPosition());
    //        AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllAirships.get(plotId);
    //        if(airshipEntity!=null) {
    //            BlockPos anchorPos = AirshipManager.getPlotPosFromId(plotId);
    //            viewProjection.popPose();
    //            viewProjection.pushPose();
    //            Vector3d v = airshipEntity .position().add(entity.position()).subtract(new Vector3d(anchorPos.getX(),anchorPos.getY(),anchorPos.getZ()));
    //            viewProjection.translate(v.x,v.y,v.z);
    //        }
    //    }
    //}
    /**
     * @author Eriksonn
     */
    //@Overwrite(remap = false)
    //public static void translateToEntity(Matrix4f matrix, Entity entity, float partialTicks) {
    //    double x = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
    //    double y = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
    //    double z = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());
//
    //    if(entity instanceof ControlledContraptionEntity && entity.level.dimension() == AirshipDimensionManager.WORLD_ID) {
    //        int plotId = AirshipManager.getIdFromPlotPos(entity.blockPosition());
    //        AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllAirships.get(plotId);
    //        if(airshipEntity!=null) {
    //            BlockPos anchorPos = AirshipManager.getPlotPosFromId(plotId);
    //            //viewProjection.popPose();
    //            //viewProjection.pushPose();
    //            Vector3d v = airshipEntity.position().add(entity.position()).subtract(new Vector3d(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ()));
    //            //viewProjection.translate(v.x-camX,v.y-camY,v.z-camZ);
    //            x = v.x;
    //            y = v.y;
    //            z = v.z;
    //        }
    //    }
//
//
    //    matrix.setTranslation((float) x, (float) y, (float) z);
    //}
}
