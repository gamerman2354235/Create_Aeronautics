package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.foundation.collision.Matrix3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value= ContraptionCollider.class,remap=false)
public class ContraptionColliderMixin {
    @Shadow(remap=false)
    public static Vector3d getWorldToLocalTranslation(Entity entity, Vector3d anchorVec, Matrix3d rotationMatrix, float yawOffset) {
        return null;
    }

    @Redirect(remap=false,method = "collideEntities",at=@At(value = "INVOKE",target = "Lcom/simibubi/create/content/contraptions/components/structureMovement/ContraptionCollider;getWorldToLocalTranslation(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/vector/Vector3d;Lcom/simibubi/create/foundation/collision/Matrix3d;F)Lnet/minecraft/util/math/vector/Vector3d;"))
    private static Vector3d collideEntitiesMixin(Entity entity, Vector3d anchorVec, Matrix3d rotationMatrix, float yawOffset,AbstractContraptionEntity contraptionEntity)
    {
        Vector3d position = getWorldToLocalTranslation(entity, anchorVec, rotationMatrix, yawOffset);
        if(contraptionEntity instanceof AirshipContraptionEntity)
        {
            AirshipContraptionEntity airshipEntity=(AirshipContraptionEntity)contraptionEntity;
            position=position.add(airshipEntity.centerOfMassOffset);
        }
        return position;
    }
}
