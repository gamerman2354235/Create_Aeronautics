package com.eriksonn.createaeronautics.world;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Fake client world used for airship contraption sync
 */
public class FakeAirshipClientWorld extends ClientWorld {
    public AirshipContraptionEntity airship;
    FakeChunkProvider chunkProvider;

    public FakeAirshipClientWorld(AirshipContraptionEntity airship, ClientPlayNetHandler p_i242067_1_, ClientWorldInfo p_i242067_2_, RegistryKey<World> p_i242067_3_, DimensionType p_i242067_4_, int p_i242067_5_, Supplier<IProfiler> p_i242067_6_, WorldRenderer p_i242067_7_, boolean p_i242067_8_, long p_i242067_9_) {
        super(p_i242067_1_, p_i242067_2_, p_i242067_3_, p_i242067_4_, p_i242067_5_, p_i242067_6_, p_i242067_7_, p_i242067_8_, p_i242067_9_);
        this.airship = airship;
        this.chunkProvider = new FakeChunkProvider(this);
    }

    @Override
    public ClientChunkProvider getChunkSource() {
        return chunkProvider;
    }

    @Override
    public void markAndNotifyBlock(BlockPos pPos, @Nullable Chunk chunk, BlockState blockstate, BlockState pState, int pFlags, int pRecursionLeft) {
        super.markAndNotifyBlock(pPos, chunk, blockstate, pState, pFlags, pRecursionLeft);

        BlockPos plotPos = AirshipManager.getPlotPosFromId(airship.plotId);
        airship.airshipContraption.setBlockState(pPos.offset(0, -plotPos.getY(), 0), pState, pState.hasTileEntity() ? getBlockEntity(pPos) : null);


        airship.invalid = true;
//        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ContraptionRenderDispatcher.invalidate(airship.airshipContraption));
    }


    /**
     * Flags are as in setBlockState
     */
    public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
        BlockPos plotPos = AirshipManager.getPlotPosFromId(airship.plotId);
        airship.airshipContraption.setBlockState(pPos.offset(0, -plotPos.getY(), 0), pNewState, pNewState.hasTileEntity() ? getBlockEntity(pPos) : null);


        airship.invalid = true;
//        this.levelRenderer.blockChanged(this, pPos, pOldState, pNewState, pFlags);
    }

    public void setBlocksDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState) {
//        this.levelRenderer.setBlockDirty(pBlockPos, pOldState, pNewState);
    }

    public void setSectionDirtyWithNeighbors(int pSectionX, int pSectionY, int pSectionZ) {
//        this.levelRenderer.setSectionDirtyWithNeighbors(pSectionX, pSectionY, pSectionZ);
    }

    public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
//        this.levelRenderer.destroyBlockProgress(pBreakerId, pPos, pProgress);
    }

    public void globalLevelEvent(int pId, BlockPos pPos, int pData) {
//        this.levelRenderer.globalLevelEvent(pId, pPos, pData);
    }

    public void levelEvent(@Nullable PlayerEntity pPlayer, int pType, BlockPos pPos, int pData) {
        try {
//            this.levelRenderer.levelEvent(pPlayer, pType, pPos, pData);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Playing level event");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Level event being played");
            crashreportcategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(pPos));
            crashreportcategory.setDetail("Event source", pPlayer);
            crashreportcategory.setDetail("Event type", pType);
            crashreportcategory.setDetail("Event data", pData);
            throw new ReportedException(crashreport);
        }
    }

    public Vector3d translateToWorldSpace(Vector3d vec) {
        int plotID = AirshipManager.getIdFromPlotPos(new BlockPos(vec));
        BlockPos plotPos = AirshipManager.getPlotPosFromId(plotID);

        return airship.toGlobalVector(new Vector3d(vec.x, vec.y - plotPos.getY(), vec.z), 1.0f);
    }

    @Override
    public void playSound(@Nullable PlayerEntity pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pX, pY, pZ));
        airship.level.playSound(pPlayer, translated.x, translated.y, translated.z, pSound, pCategory, pVolume, pPitch);
    }

    @Override
    public void playSound(@Nullable PlayerEntity pPlayer, Entity pEntity, SoundEvent pEvent, SoundCategory pCategory, float pVolume, float pPitch) {
        airship.level.playSound(pPlayer, pEntity, pEvent, pCategory, pVolume, pPitch);
    }

    @Override
    public void playLocalSound(BlockPos pPos, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pPos.getX(), pPos.getY(), pPos.getZ()));
        ((ClientWorld) airship.level).playLocalSound(new BlockPos(translated), pSound, pCategory, pVolume, pPitch, pDistanceDelay);
    }

    @Override
    public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pX, pY, pZ));
        airship.level.playLocalSound(translated.x, translated.y, translated.z, pSound, pCategory, pVolume, pPitch, pDistanceDelay);
    }

    @Override
    public void playSound(@Nullable PlayerEntity pPlayer, BlockPos pPos, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pPos.getX(), pPos.getY(), pPos.getZ()));

        airship.level.playSound(pPlayer, new BlockPos(translated), pSound, pCategory, pVolume, pPitch);
    }

    @Override
    public void addParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pX, pY, pZ));
        Vector3d translatedSpeed = airship.applyRotation(new Vector3d(pXSpeed, pYSpeed, pZSpeed), 1.0f);

        airship.level.addParticle(pParticleData, translated.x, translated.y, translated.z, translatedSpeed.x, translatedSpeed.y, translatedSpeed.z);
    }

    @Override
    public void addParticle(IParticleData pParticleData, boolean pForceAlwaysRender, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pX, pY, pZ));
        Vector3d translatedSpeed = airship.applyRotation(new Vector3d(pXSpeed, pYSpeed, pZSpeed), 1.0f);

        airship.level.addParticle(pParticleData, pForceAlwaysRender, translated.x, translated.y, translated.z, translatedSpeed.x, translatedSpeed.y, translatedSpeed.z);
    }

    public void addAlwaysVisibleParticle(IParticleData pParticleData, boolean pIgnoreRange, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pX, pY, pZ));
        Vector3d translatedSpeed = airship.applyRotation(new Vector3d(pXSpeed, pYSpeed, pZSpeed), 1.0f);

        airship.level.addAlwaysVisibleParticle(pParticleData, pIgnoreRange, translated.x, translated.y, translated.z, translatedSpeed.x, translatedSpeed.y, translatedSpeed.z);
    }

    public void addAlwaysVisibleParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        Vector3d translated = translateToWorldSpace(new Vector3d(pX, pY, pZ));
        Vector3d translatedSpeed = airship.applyRotation(new Vector3d(pXSpeed, pYSpeed, pZSpeed), 1.0f);

        airship.level.addAlwaysVisibleParticle(pParticleData, translated.x, translated.y, translated.z, translatedSpeed.x, translatedSpeed.y, translatedSpeed.z);
    }
}
