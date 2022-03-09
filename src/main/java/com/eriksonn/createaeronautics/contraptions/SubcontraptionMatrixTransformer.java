package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.utils.AbstractContraptionEntityExtension;
import com.eriksonn.createaeronautics.world.FakeAirshipClientWorld;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

public class SubcontraptionMatrixTransformer {

    public static void setupTransforms(AbstractContraptionEntity entity, MatrixStack model) {
        if(entity instanceof ControlledContraptionEntity && entity.level instanceof FakeAirshipClientWorld)
        {
            int plotId = AirshipManager.getIdFromPlotPos(((FakeAirshipClientWorld) entity.level).airship.blockPosition());
            AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllClientAirships.get(plotId);
            if(airshipEntity!=null) {
                BlockPos clientWorldOffset = AirshipManager.getPlotPosFromId(plotId);
                Vector3d airshipPosition = airshipEntity.getAnchorVec();


                Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
                model.translate(0, -clientWorldOffset.getY(), 0);

                model.translate(airshipPosition.x, airshipPosition.y, airshipPosition.z);

                // get local position
                Vector3d localPosition = entity.getAnchorVec().subtract(0, clientWorldOffset.getY(), 0);

                model.translate(-localPosition.x, -localPosition.y, -localPosition.z);

                Vector3d centerOfMassOffset = airshipEntity.applyRotation(airshipEntity.centerOfMassOffset, 1.0f);
                model.translate(-centerOfMassOffset.x, -centerOfMassOffset.y, -centerOfMassOffset.z);


                model.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);

                // rotate
                Quaternion Q = airshipEntity.quat.copy();
                Q.conj();
                model.mulPose(Q);

                Vector3d postRotationOffset = airshipEntity.applyRotation(rotationOffset, 1.0f);
                model.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
//                model.translate(-postRotationOffset.x, -postRotationOffset.y, -postRotationOffset.z);
//                model.translate(rotat.x, postRotationOffset.y, postRotationOffset.z);


                // translate again by local position
                model.translate(localPosition.x, localPosition.y, localPosition.z);
//                model.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
//                Vector3d postCenterOfMassOffset = airshipEntity.reverseRotation(centerOfMassOffset, 1.0f);
                Vector3d postCenterOfMassOffset = centerOfMassOffset;



//
//                model.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
//                model.translate(globalPos.x,globalPos.y,globalPos.z);

//                Vector3d newPos = ((AbstractContraptionEntityExtension) entity).createAeronautics$getOriginalPosition();
                //entity.setPos(newPos.x,newPos.y,newPos.z);
            }
        }
    }
}
