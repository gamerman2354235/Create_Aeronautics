package com.eriksonn.createaeronautics.dimension;
//import com.qouteall.immersive_portals.ducks.IEWorld;
import com.eriksonn.createaeronautics.dimension.IEWorld;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.mixins.WorldAccessorMixin;
import com.mojang.authlib.GameProfile;
import com.qouteall.immersive_portals.render.context_management.DimensionRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientDimensionManager {
    private static AirshipClientWorld clientWorld;
    static boolean isInitialized=false;
    ClientDimensionManager()
    {

    }
    static void generateWorld()
    {

    }
    public static void initializeIfNeeded() {
        if (!isInitialized) {
            Validate.isTrue(client.level != null);
            Validate.isTrue(client.levelRenderer != null);
            Validate.notNull(client.player);
            Validate.isTrue(client.player.level == client.level);
            RegistryKey<World> playerDimension = client.level.dimension();
            isInitialized = true;
        }

    }
    public static AirshipClientWorld getWorld() {
        //initializeIfNeeded();
        return clientWorld==null ? createSecondaryClientWorld(AirshipDimensionManager.WORLD_ID) : clientWorld;
    }

    private static final Minecraft client = Minecraft.getInstance();
    private static AirshipClientWorld createSecondaryClientWorld(RegistryKey<World> dimension) {
        Validate.isTrue(client.player.level.dimension() != dimension);
        //isCreatingClientWorld = true;
        client.getProfiler().push("create_world");
        int chunkLoadDistance = 3;
        WorldRenderer worldRenderer = new WorldRenderer(client, client.renderBuffers());

        AirshipClientWorld newWorld;
        try {
            ClientPlayNetHandler newNetworkHandler = new ClientPlayNetHandler(client, new ChatScreen("You should not be seeing me. I'm just a faked screen."), new NetworkManager(PacketDirection.CLIENTBOUND), new GameProfile((UUID)null, "faked_profiler_id"));
            ClientPlayNetHandler mainNetHandler = client.player.connection;
            //((IEClientPlayNetworkHandler)newNetworkHandler).setPlayerListEntries(((IEClientPlayNetworkHandler)mainNetHandler).getPlayerListEntries());
            RegistryKey<DimensionType> dimensionTypeKey = AirshipDimensionManager.DIMENSION_TYPE_ID;
            ISpawnWorldInfo I = ((WorldAccessorMixin)client.level).getLevelData();
            ClientWorld.ClientWorldInfo currentProperty = (ClientWorld.ClientWorldInfo)I;
            DynamicRegistries dimensionTracker = mainNetHandler.registryAccess();
            //((IEClientPlayNetworkHandler)newNetworkHandler).portal_setRegistryManager(dimensionTracker);
            DimensionType dimensionType = (DimensionType)dimensionTracker.dimensionTypes().get(dimensionTypeKey);
            ClientWorld.ClientWorldInfo properties = new ClientWorld.ClientWorldInfo(currentProperty.getDifficulty(), currentProperty.isHardcore(), currentProperty.getHorizonHeight() < 1.0D);
            newWorld = new AirshipClientWorld(newNetworkHandler, properties, dimension, dimensionType, chunkLoadDistance, () -> {
                return client.getProfiler();
            }, worldRenderer, client.level.isDebug(), 0);
        } catch (Exception var11) {
            throw new IllegalStateException("Creating Client World " + dimension, var11);
        }

        //worldRenderer.setLevel(newWorld);
        //worldRenderer.onResourceManagerReload(client.getResourceManager());
        //((IEClientPlayNetworkHandler)((IEClientWorld)newWorld).getNetHandler()).setWorld(newWorld);
        //clientWorldMap.put(dimension, newWorld);
        //worldRendererMap.put(dimension, worldRenderer);
        //Helper.log("Client World Created " + dimension.location());
        //isCreatingClientWorld = false;
        //clientWorldLoadSignal.emit(newWorld);
        clientWorld=newWorld;
        client.getProfiler().pop();
        return newWorld;
    }
}
