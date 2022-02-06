package com.eriksonn.createaeronautics.mixins;

import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.LinearActuatorTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({MechanicalBearingTileEntity.class})
public interface ContraptionHolderAccessor {
    @Accessor(remap = false)
    ControlledContraptionEntity getMovedContraption();

}
