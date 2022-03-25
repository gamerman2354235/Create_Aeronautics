package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.blocks.propeller_bearing.MecanicalBearingTileEntityExtension;
import com.eriksonn.createaeronautics.utils.BearingContraptionExtension;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MechanicalBearingTileEntity.class)
public class MechanicalBearingTileEntityMixin implements MecanicalBearingTileEntityExtension {
    @Redirect(
            method = "assemble()V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/components/structureMovement/bearing/BearingContraption;assemble(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", remap = false),
            remap = false
    )
    private boolean onTryAssemble(BearingContraption contraption, World world, BlockPos pos) throws AssemblyException {
        if (isPropeller()) ((BearingContraptionExtension) contraption).setPropeller();
        return contraption.assemble(world, pos);
    }

    @Override
    public boolean isPropeller() {
        return false;
    }
}
