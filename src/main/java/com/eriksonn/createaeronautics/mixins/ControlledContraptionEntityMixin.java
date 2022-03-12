package com.eriksonn.createaeronautics.mixins;

import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ControlledContraptionEntity.class)
public interface ControlledContraptionEntityMixin {

    @Invoker("makeStructureTransform")
    public StructureTransform invokeMakeStructureTransform();
}
