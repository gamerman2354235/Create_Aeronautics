package com.eriksonn.createaeronautics.network;


import com.eriksonn.createaeronautics.dimension.ClientDimensionManager;
import com.eriksonn.createaeronautics.dimension_sync.CHelper;
import com.eriksonn.createaeronautics.mixins.ClientWorldAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import java.util.Optional;

public class CommonNetworkClient {

        public static final Minecraft client = Minecraft.getInstance();
        //private static final LimitedLogger limitedLogger = new LimitedLogger(100);
        static boolean isProcessingRedirectedMessage = false;

        public CommonNetworkClient() {
        }

        public static void processRedirectedPacket(RegistryKey<World> dimension, IPacket packet) {
            Runnable func = () -> {
                try {
                    client.getProfiler().push("process_redirected_packet");
                    ClientWorld packetWorld = ClientDimensionManager.getWorld();
                    doProcessRedirectedMessage(packetWorld, packet);
                } finally {
                    client.getProfiler().pop();
                }

            };
            CHelper.executeOnRenderThread(func);
        }

        public static void doProcessRedirectedMessage(ClientWorld packetWorld, IPacket packet) {
            boolean oldIsProcessing = isProcessingRedirectedMessage;
            isProcessingRedirectedMessage = true;
            ClientPlayNetHandler netHandler = ((ClientWorldAccessorMixin)packetWorld).getConnection();
            if (netHandler.getLevel() != packetWorld) {
                //((IEClientPlayNetworkHandler)netHandler).setWorld(packetWorld);
                //Helper.err("The world field of client net handler is wrong");
            }

            client.getProfiler().push(() -> {
                return "handle_redirected_packet" + packetWorld.dimension() + packet;
            });

            try {
                withSwitchedWorld(packetWorld, () -> {
                    packet.handle(netHandler);
                });
            } catch (Throwable var8) {
                //limitedLogger.throwException(() -> {
                    throw new IllegalStateException("handling packet in " + packetWorld.dimension(), var8);
                //});
            } finally {
                client.getProfiler().pop();
                isProcessingRedirectedMessage = oldIsProcessing;
            }

        }

        public static void withSwitchedWorld(ClientWorld newWorld, Runnable runnable) {
            Validate.isTrue(client.isSameThread());
            ClientWorld originalWorld = client.level;
            WorldRenderer originalWorldRenderer = client.levelRenderer;
            //WorldRenderer newWorldRenderer = ClientWorldLoader.getWorldRenderer(newWorld.dimension());
            //Validate.notNull(newWorldRenderer);
            client.level = newWorld;
            //((IEParticleManager)client.particleEngine).mySetWorld(newWorld);
            //((IEMinecraftClient)client).setWorldRenderer(newWorldRenderer);

            try {
                runnable.run();
            } finally {
                if (client.level != newWorld) {
                    //Helper.err("Respawn packet should not be redirected");
                    originalWorld = client.level;
                    originalWorldRenderer = client.levelRenderer;
                    throw new RuntimeException("Respawn packet should not be redirected");
                }

                client.level = originalWorld;
                //((IEMinecraftClient)client).setWorldRenderer(originalWorldRenderer);
                //((IEParticleManager)client.particleEngine).mySetWorld(originalWorld);
            }

        }

        public static void processEntitySpawn(String entityTypeString, int entityId, RegistryKey<World> dim, CompoundNBT compoundTag) {
            Optional<EntityType<?>> entityType = EntityType.byString(entityTypeString);
            if (!entityType.isPresent()) {
                //Helper.err("unknown entity type " + entityTypeString);
            } else {
                CHelper.executeOnRenderThread(() -> {
                    client.getProfiler().push("ip_spawn_entity");
                    ClientWorld world = ClientDimensionManager.getWorld();
                    Entity entity = ((EntityType)entityType.get()).create(world);
                    entity.load(compoundTag);
                    entity.setId(entityId);
                    entity.setPacketCoordinates(entity.getX(), entity.getY(), entity.getZ());
                    world.putNonPlayerEntity(entityId, entity);
                    //if (entity instanceof Portal) {
                    //    ClientWorldLoader.getWorld(((Portal)entity).dimensionTo);
                    //    clientPortalSpawnSignal.emit((Portal)entity);
                    //}

                    client.getProfiler().pop();
                });
            }
        }
}
