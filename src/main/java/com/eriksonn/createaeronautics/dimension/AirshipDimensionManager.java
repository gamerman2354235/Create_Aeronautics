package com.eriksonn.createaeronautics.dimension;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

public final class AirshipDimensionManager {
    public static final AirshipDimensionManager INSTANCE = new AirshipDimensionManager();

    static String MOD_ID = "createaeronautics";
    static ResourceLocation makeId(String id) {
        return new ResourceLocation(MOD_ID, id);
    }
    public ServerWorld getWorld() {
        MinecraftServer server = getServer();
        ServerWorld world = server.getLevel(WORLD_ID);

        if (world == null) {
            throw new IllegalStateException("The airship world is missing.");
        }
        return world;
    }
    public AirshipWorldData getWorldData() {
        return getWorld().getChunkSource().getDataStorage().computeIfAbsent(AirshipWorldData::new, AirshipWorldData.ID);
    }
    public AirshipDimensionPlot allocatePlot(BlockPos size) {
        AirshipDimensionPlot plot = getWorldData().allocatePlot(size);
        //AELog.info("Allocating storage cell plot %d with size %s for %d", plot.getId(), size, ownerId);
        return plot;
    }
    //public static final RegistryKey<DimensionType> DIMENSION_TYPE_ID = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, makeId("airship_storage"));
    //public static final RegistryKey<World> WORLD_ID = RegistryKey.create(Registry.DIMENSION_REGISTRY, makeId("airship_storage"));
    //public static final RegistryKey<Dimension> DIMENSION_ID = RegistryKey.create(Registry.LEVEL_STEM_REGISTRY, makeId("airship_storage"));
    //public static final RegistryKey<Biome> BIOME_KEY = RegistryKey.create(Registry.BIOME_REGISTRY, makeId("airship_storage"));

    public static final RegistryKey<DimensionType> DIMENSION_TYPE_ID;
    public static final ResourceLocation CHUNK_GENERATOR_ID;
    public static final RegistryKey<Biome> BIOME_KEY;
    public static final RegistryKey<Dimension> DIMENSION_ID;
    public static final RegistryKey<World> WORLD_ID;
    public static ResourceLocation SKY_PROPERTIES_ID;


    static {
        DIMENSION_TYPE_ID = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, makeId("airship_storage"));
        CHUNK_GENERATOR_ID = makeId("airship_storage");
        BIOME_KEY = RegistryKey.create(Registry.BIOME_REGISTRY, makeId("airship_storage"));
        DIMENSION_ID = RegistryKey.create(Registry.LEVEL_STEM_REGISTRY, makeId("airship_storage"));
        WORLD_ID = RegistryKey.create(Registry.DIMENSION_REGISTRY, makeId("airship_storage"));
        SKY_PROPERTIES_ID = makeId("airship_storage");
    }


    private static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
    private AirshipDimensionManager() {
    }
}
