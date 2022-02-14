package com.eriksonn.createaeronautics.mixins;

import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Much neater way to access the assembleNextTick variable.
 * (Compared to the old way of placing a class into the same package)
 * @author FortressNebula
 */
@Mixin(MechanicalBearingTileEntity.class)
public interface MechanicalBearingTileEntityAccessor {
    @Accessor(remap = false) void setAssembleNextTick(boolean val);
}
