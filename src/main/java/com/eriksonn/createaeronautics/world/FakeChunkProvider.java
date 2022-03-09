package com.eriksonn.createaeronautics.world;

import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;
import java.util.HashMap;

public class FakeChunkProvider extends ClientChunkProvider {

    private final WorldLightManager lightEngine;
    public FakeAirshipClientWorld level;

    public FakeChunkProvider(FakeAirshipClientWorld level) {
        super(level, 5);
        this.level = level;
        this.lightEngine = new WorldLightManager(this, true, true);
    }



    @Nullable
    @Override
    public Chunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
        Chunk chunk = chunkMap.get(ChunkPos.asLong(pChunkX, pChunkZ));
        if(chunk == null || !chunk.getPos().equals(new ChunkPos(pChunkX, pChunkZ))) {
            chunk = new Chunk(level, new ChunkPos(pChunkX, pChunkZ), null);
            chunkMap.put(ChunkPos.asLong(pChunkX, pChunkZ), chunk);
        }
        return chunk;
    }

    public HashMap<Long, Chunk> chunkMap = new HashMap<>();

    @Override
    public String gatherStats() {
        return "";
    }

    @Override
    public WorldLightManager getLightEngine() {
        return lightEngine;
    }

    @Override
    public IBlockReader getLevel() {
        return level;
    }
}
