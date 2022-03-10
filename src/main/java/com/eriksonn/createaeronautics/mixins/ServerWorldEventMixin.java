package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * TODO: Fix ALL of this to not use mixins and instead use a subclass, and mixin to where the world is created
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldEventMixin {

    @Shadow private boolean tickingEntities;

    @Shadow @Final private Queue<Entity> toAddAfterTick;

    @Shadow @Final private Int2ObjectMap<Entity> entitiesById;

    @Shadow @Final private Map<UUID, Entity> entitiesByUuid;

    @Shadow public abstract ServerChunkProvider getChunkSource();

    @Shadow @Final private Set<PathNavigator> navigations;

    @Shadow protected abstract boolean addEntity(Entity pEntity);

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "sendBlockUpdated", remap = false, at = @At("HEAD"))
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        if (((Object) this) instanceof ServerWorld && ((ServerWorld) (Object) this).dimension() == AirshipDimensionManager.WORLD_ID) {
            int id = AirshipManager.getIdFromPlotPos(pos);
            try {
                AirshipManager.INSTANCE.AllAirships.get(id).stcQueueBlockUpdate(pos.offset(-64, -64, -64));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author RyanHCode
     */
    @Overwrite(remap = false)
    public boolean addFreshEntity(Entity entity) {
        if (
                ((Object) this) instanceof ServerWorld && ((ServerWorld) (Object) this).dimension() == AirshipDimensionManager.WORLD_ID
                        && !(entity instanceof AbstractContraptionEntity || entity instanceof SuperGlueEntity)
        ) {
            // get position of entity
            Vector3d pos = entity.position();
            BlockPos bPos = entity.blockPosition();

            // get airship plot id
            int id = AirshipManager.getIdFromPlotPos(bPos);
            BlockPos plotPos = AirshipManager.getPlotPosFromId(id);

            // get airship
            AirshipContraptionEntity airship = AirshipManager.INSTANCE.AllAirships.get(id);

            if (airship != null) {
                Vector3d position = pos;
                position = position.subtract(plotPos.getX(), plotPos.getY(), plotPos.getZ());
                position = airship.toGlobalVector(position, 1.0f);

                // rotate entity motion
                entity.setDeltaMovement(airship.applyRotation(entity.getDeltaMovement(), 1.0f));

                entity.setPosRaw(position.x, position.y, position.z);
                entity.setLevel(airship.level);
                return airship.level.addFreshEntity(entity);
            }
        }

        return this.addEntity(entity);
    }


    /**
     * @author RyanHCode
     */
    @Overwrite(remap = false)
    public void playSound(@Nullable PlayerEntity pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
        if (
                ((Object) this) instanceof ServerWorld && ((ServerWorld) (Object) this).dimension() == AirshipDimensionManager.WORLD_ID
        ) {
            // get position of entity
            Vector3d pos = new Vector3d(pX, pY, pZ);
            BlockPos bPos = new BlockPos(pos);

            // get airship plot id
            int id = AirshipManager.getIdFromPlotPos(bPos);
            BlockPos plotPos = AirshipManager.getPlotPosFromId(id);

            // get airship
            AirshipContraptionEntity airship = AirshipManager.INSTANCE.AllAirships.get(id);

            if (airship != null) {
                Vector3d position = new Vector3d(bPos.getX(), bPos.getY(), bPos.getZ());
                position = position.subtract(plotPos.getX(), plotPos.getY(), plotPos.getZ());
                position = airship.toGlobalVector(position, 1.0f);

                airship.level.playSound(pPlayer, position.x, position.y, position.z, pSound, pCategory, pVolume, pPitch);
                return;
            }
        }

        net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pPlayer, pSound, pCategory, pVolume, pPitch);
        if (event.isCanceled() || event.getSound() == null) return;
        pSound = event.getSound();
        pCategory = event.getCategory();
        pVolume = event.getVolume();
        server.getPlayerList().broadcast(pPlayer, pX, pY, pZ, pVolume > 1.0F ? (double)(16.0F * pVolume) : 16.0D, ((ServerWorld) (Object) this).dimension(), new SPlaySoundEffectPacket(pSound, pCategory, pX, pY, pZ, pVolume, pPitch));
    }

    /**
     * @author RyanHCode
     */
    @Overwrite(remap = false)
    private boolean sendParticles(ServerPlayerEntity pPlayer, boolean pLongDistance, double pPosX, double pPosY, double pPosZ, IPacket<?> pPacket) {
        if (pPlayer.getLevel() != ((ServerWorld) (Object) this)) {
            if (
                    ((Object) this) instanceof ServerWorld && ((ServerWorld) (Object) this).dimension() == AirshipDimensionManager.WORLD_ID
            ) {
                // get position of entity
                Vector3d pos = new Vector3d(pPosX, pPosY, pPosZ);
                BlockPos bPos = new BlockPos(pos);

                // get airship plot id
                int id = AirshipManager.getIdFromPlotPos(bPos);
                BlockPos plotPos = AirshipManager.getPlotPosFromId(id);

                // get airship
                AirshipContraptionEntity airship = AirshipManager.INSTANCE.AllAirships.get(id);

                if (airship != null) {
                    Vector3d position = new Vector3d(bPos.getX(), bPos.getY(), bPos.getZ());
                    position = position.subtract(plotPos.getX(), plotPos.getY(), plotPos.getZ());
                    position = airship.toGlobalVector(position, 1.0f);

                    BlockPos blockpos = pPlayer.blockPosition();

                    SSpawnParticlePacket newPacket = null;
                    if(pPacket instanceof SSpawnParticlePacket) {
                        SSpawnParticlePacket spawnPacket = (SSpawnParticlePacket) pPacket;
                        newPacket = new SSpawnParticlePacket(spawnPacket.getParticle(), pLongDistance, position.x, position.y, position.z, spawnPacket.getXDist(), spawnPacket.getYDist(), spawnPacket.getZDist(), spawnPacket.getMaxSpeed(), spawnPacket.getCount());
                    }

                    if (blockpos.closerThan(position, pLongDistance ? 512.0D : 32.0D)) {
                        pPlayer.connection.send(newPacket != null ? newPacket : pPacket);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        } else {
            BlockPos blockpos = pPlayer.blockPosition();
            if (blockpos.closerThan(new Vector3d(pPosX, pPosY, pPosZ), pLongDistance ? 512.0D : 32.0D)) {
                pPlayer.connection.send(pPacket);
                return true;
            } else {
                return false;
            }
        }
    }
}

