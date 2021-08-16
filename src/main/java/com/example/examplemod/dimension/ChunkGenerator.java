package com.example.examplemod.dimension;

import appeng.spatial.SpatialStorageChunkGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class ChunkGenerator extends net.minecraft.world.gen.ChunkGenerator {
    /**
     * This codec is necessary to restore the actual instance of the Biome we use, since it is sources from the dynamic
     * registries and <em>must be the same object as in the registry!</em>.
     * <p>
     * If it was not the same object, then the Object->ID lookup would fail since it uses an identity hashmap
     * internally.
     */
    public static final Codec<ChunkGenerator> CODEC = RegistryLookupCodec
            .create(Registry.BIOME_REGISTRY)
            .xmap(ChunkGenerator::new, ChunkGenerator::getBiomeRegistry).stable().codec();
    private final Registry<Biome> biomeRegistry;
    private final Blockreader columnSample;

    //private final BlockState defaultBlockState;

    public ChunkGenerator(Registry<Biome> biomeRegistry) {
        super(createBiomeSource(biomeRegistry), createSettings());
        //this.defaultBlockState = .getDefaultState();
        this.biomeRegistry = biomeRegistry;

        // Vertical sample is mostly used for Feature generation, for those purposes
        // we're all filled with matrix blocks
        BlockState[] columnSample = new BlockState[256];
        //Arrays.fill(columnSample, this.defaultBlockState);
        this.columnSample = new Blockreader(columnSample);
    }
    @Override
    protected Codec<? extends net.minecraft.world.gen.ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public net.minecraft.world.gen.ChunkGenerator withSeed(long p_230349_1_) {
        return this;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion region, IChunk chunk) {
        this.fillChunk(chunk);
        chunk.setUnsaved(false);
    }

    private void fillChunk(IChunk chunk) {
        BlockPos.Mutable mutPos = new BlockPos.Mutable();
        for (int cx = 0; cx < 16; cx++) {
            mutPos.setX(cx);
            for (int cz = 0; cz < 16; cz++) {
                // FIXME: It's likely a bad idea to fill Y in the inner-loop given the storage
                // layout of chunks
                mutPos.setZ(cz);
                for (int cy = 0; cy < 256; cy++) {
                    mutPos.setY(cy);
                    //chunk.setBlockState(mutPos, defaultBlockState, false);
                }
            }
        }
    }
    @Override
    public void fillFromNoise(IWorld world, StructureManager accessor, IChunk chunk) {
    }
    @Override
    public IBlockReader getBaseColumn(int x, int z) {
        return columnSample;
    }
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Type heightmapType) {
        return 0;
    }

    private static SingleBiomeProvider createBiomeSource(Registry<Biome> biomeRegistry) {
        return new SingleBiomeProvider(biomeRegistry.getOrThrow(Biomes.THE_VOID));
    }
    public Registry<Biome> getBiomeRegistry() {
        return biomeRegistry;
    }
    private static DimensionStructuresSettings createSettings() {
        return new DimensionStructuresSettings(Optional.empty(), Collections.emptyMap());
    }
}
