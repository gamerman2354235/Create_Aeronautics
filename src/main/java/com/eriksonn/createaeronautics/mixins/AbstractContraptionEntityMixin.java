package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.utils.AbstractContraptionEntityExtension;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value= AbstractContraptionEntity.class)
public class AbstractContraptionEntityMixin implements AbstractContraptionEntityExtension {
    @Unique
    Vector3d createAeronautics$originalPosition;
    public void createAeronautics$setOriginalPosition(Vector3d pos)
    {
        createAeronautics$originalPosition=pos;
    }
    public Vector3d createAeronautics$getOriginalPosition()
    {
        return createAeronautics$originalPosition;
    }
}
