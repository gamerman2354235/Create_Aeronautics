package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldEventMixin {

    @Inject(method = "sendBlockUpdated", remap = false, at = @At("HEAD"))
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        if (((Object) this) instanceof ServerWorld && ((ServerWorld) (Object) this).dimension() == AirshipDimensionManager.WORLD_ID) {
            int id = AirshipManager.getIdFromPlotPos(pos);
            try {
                AirshipManager.INSTANCE.AllAirships.get(id).stcHandleBlockUpdate(pos.offset(-64, -64, -64));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

