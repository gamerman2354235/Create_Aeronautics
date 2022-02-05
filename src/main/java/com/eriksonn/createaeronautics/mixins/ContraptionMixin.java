package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.blocks.propeller_bearing.PropellerBearingBlock;
import com.eriksonn.createaeronautics.index.CABlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Mixin(value = Contraption.class)
public class ContraptionMixin {

    @Inject(locals = LocalCapture.CAPTURE_FAILHARD,remap=false,method = "moveBlock", at = @At(remap=false,value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/components/structureMovement/Contraption;addBlock(Lnet/minecraft/util/math/BlockPos;Lorg/apache/commons/lang3/tuple/Pair;)V"))
    protected void onMoveBlock(World world, Direction forcedDirection, Queue frontier, Set visited, CallbackInfoReturnable<Boolean> cir, BlockPos pos, BlockState state) {

        if (CABlocks.PROPELLER_BEARING.has(state)) {
            Direction facing = state.getValue(PropellerBearingBlock.FACING);
            BlockPos offset = pos.relative(facing);
            if (!visited.contains(offset))
                frontier.add(offset);
            return;

        }
    }
}
