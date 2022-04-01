package com.eriksonn.createaeronautics.mixins;

import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ControlledContraptionEntity.class)
public interface ControlledContraptionEntityMixin {

    @Invoker(value = "makeStructureTransform", remap = false)
    public StructureTransform invokeMakeStructureTransform();

    @Accessor(remap = false)
    BlockPos getControllerPos();
}
