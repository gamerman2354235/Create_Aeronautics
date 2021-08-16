package com.example.examplemod.index;


import appeng.mixins.spatial.DimensionTypeMixin;
import com.example.examplemod.dimension.AirshipDimensionManager;
import com.example.examplemod.dimension.ChunkGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;

public final class CADimensions {
    public CADimensions()
    {}
    public void registerDimension(RegistryEvent.NewRegistry e) {
        Registry.register(Registry.CHUNK_GENERATOR, AirshipDimensionManager.CHUNK_GENERATOR_ID, ChunkGenerator.CODEC);
    }
}
