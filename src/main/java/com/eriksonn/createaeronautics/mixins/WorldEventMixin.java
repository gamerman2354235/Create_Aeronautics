package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import net.minecraft.block.BlockState;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ServerWorld.class)
public abstract class WorldEventMixin extends World{

    protected WorldEventMixin(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_) {
        super(p_i241925_1_, p_i241925_2_, p_i241925_3_, p_i241925_4_, p_i241925_5_, p_i241925_6_, p_i241925_7_);
    }
    @Inject(method = "onBlockStateChange", at = @At("HEAD"))
    public void onBlockStateChange(BlockPos pos, BlockState first, BlockState second, CallbackInfo ci) {


        if(this.dimension()==AirshipDimensionManager.WORLD_ID)
        {
            AirshipManager.INSTANCE.blockStateChange(pos, first, second);
            //System.out.println();
            //System.out.println("world:" + this.toString());
            //System.out.println("inside airship dimension, yay!");
            //System.out.println("position:" +p_217393_1_.toString());
            //System.out.println("first state:" +p_217393_2_.toString());
            //System.out.println("second state:" +p_217393_3_.toString());
        }


    }
}
