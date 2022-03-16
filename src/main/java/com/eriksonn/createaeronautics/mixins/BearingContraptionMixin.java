package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.utils.BearingContraptionExtension;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BearingContraption.class)
public class BearingContraptionMixin implements BearingContraptionExtension {
    private boolean isPropeller = false;

    @Shadow(remap = false) protected int sailBlocks;

    @Inject(
            method = "assemble(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At(value = "RETURN", ordinal = 1),
            remap = false)
    private void onBlocksEmpty(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) throws AssemblyException {
        tryFailAssembly();
    }

    @Inject(
            method = "assemble(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At(value = "RETURN", ordinal = 2),
            remap = false)
    private void onSucceededAssemble(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) throws AssemblyException {
        tryFailAssembly();
    }

    @Override
    public void setPropeller() {
        isPropeller = true;
    }

    private void tryFailAssembly() throws AssemblyException {
        if (isPropeller && sailBlocks < 2) {
            throw new AssemblyException("not_enough_sails", sailBlocks, 2);
        }
    }
}
