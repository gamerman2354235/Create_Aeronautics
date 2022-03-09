package com.eriksonn.createaeronautics.contraptions;

import appeng.me.helpers.GenericInterestManager;
import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.mixins.ContraptionHolderAccessor;
import com.eriksonn.createaeronautics.utils.AbstractContraptionEntityExtension;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.core.jmx.Server;


import java.lang.reflect.Field;
import java.util.*;

public class AirshipManager {
    public static final AirshipManager INSTANCE = new AirshipManager();

    public Map<Integer,AirshipContraptionEntity> AllAirships;
    public Map<Integer, AirshipContraptionData> AirshipData;
    public Map<Integer,AirshipContraptionEntity> AllClientAirships = new HashMap<>();

    class AirshipContraptionData
    {
        public boolean needsUpdating=false;
        public List<AbstractContraptionEntity> addedContraptions=new ArrayList<>();
        public Map<BlockPos,BlockState> ClientBlockStateChanges=new HashMap<>();
        public Map<BlockPos,TileEntity> presentTileEntities =new HashMap<>();
        public List<TileEntity> specialRenderedTileEntitiesChanges=new ArrayList<>();
        public List<TileEntity> maybeInstancedTileEntitiesChanges=new ArrayList<>();
        public Map<BlockPos,BlockState> sails=new HashMap<>();
        public Map<BlockPos,ControlledContraptionEntity> subContraptions = new HashMap<>();
        public int removeTimer=0;//need to wait abit to prevent client render crash
    }
    AirshipManager()
    {
        AllAirships=new HashMap<>();
        AirshipData =new HashMap<>();
        MinecraftForge.EVENT_BUS.register(AirshipEventHandler.class);
    }
    public void tryAddEntity(int index ,AirshipContraptionEntity E)
    {
        AllAirships.putIfAbsent(index,E);
        AirshipData.putIfAbsent(index,new AirshipContraptionData());
    }
    public void tick() {
        int plotToRemove=-1;
        for (Map.Entry<Integer,AirshipContraptionEntity> entry: AllAirships.entrySet())
        {
            ServerWorld world = AirshipDimensionManager.INSTANCE.getWorld();
            AirshipContraptionEntity entity=entry.getValue();
            if(!entity.isAlive())
            {
                plotToRemove=entry.getKey();
            }
            AirshipContraptionData data = AirshipData.get(entry.getKey());
            BlockPos pos=getPlotPosFromId(entry.getKey());
            ChunkPos chunkPos = new ChunkPos(pos);
            if(entity.level.isLoaded(entity.blockPosition()))
            {
                ForgeChunkManager.forceChunk(world, CreateAeronautics.MODID, pos, chunkPos.x, chunkPos.z, true, true);
            }else
            {
                ForgeChunkManager.forceChunk(world, CreateAeronautics.MODID, pos, chunkPos.x, chunkPos.z, false, true);
            }
        }
        if(plotToRemove!=-1) {
            int a = AirshipData.get(plotToRemove).removeTimer++;
            if(a>5)
                removePlot(plotToRemove);
        }

    }
    public void removePlot(int id)
    {
        AirshipContraptionEntity entity = AllAirships.get(id);
        if(entity!=null) {
            System.out.println("plot remove");
            ServerWorld world = AirshipDimensionManager.INSTANCE.getWorld();
            BlockPos anchor = getPlotPosFromId(id);
            ChunkPos chunkPos = new ChunkPos(anchor);
            removeBlocksFromWorld(world, anchor, entity.airshipContraption.getBlocks());
            ForgeChunkManager.forceChunk(world, CreateAeronautics.MODID, anchor, chunkPos.x, chunkPos.z, false, true);
            AllAirships.remove(id);
            AirshipData.remove(id);
        }
    }
    public void removeBlocksFromWorld(World world, BlockPos anchor,Map<BlockPos, Template.BlockInfo> blocks) {
        //storage.values()
        //        .forEach(MountedStorage::removeStorageFromWorld);
        //fluidStorage.values()
        //        .forEach(MountedFluidStorage::removeStorageFromWorld);
        //glueToRemove.forEach(SuperGlueEntity::remove);

        for (boolean brittles : Iterate.trueAndFalse) {
            for (Iterator<Template.BlockInfo> iterator = blocks.values()
                    .iterator(); iterator.hasNext();) {
                Template.BlockInfo block = iterator.next();
                if (brittles != BlockMovementChecks.isBrittle(block.state))
                    continue;

                BlockPos add = block.pos.offset(anchor);
                BlockState oldState = world.getBlockState(add);
                Block blockIn = oldState.getBlock();
                if (block.state.getBlock() != blockIn)
                    iterator.remove();
                world.removeBlockEntity(add);
                int flags = Constants.BlockFlags.IS_MOVING | Constants.BlockFlags.NO_NEIGHBOR_DROPS | Constants.BlockFlags.UPDATE_NEIGHBORS
                        | Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.RERENDER_MAIN_THREAD;
                if (blockIn instanceof IWaterLoggable && oldState.hasProperty(BlockStateProperties.WATERLOGGED)
                        && oldState.getValue(BlockStateProperties.WATERLOGGED)) {
                    world.setBlock(add, Blocks.WATER.defaultBlockState(), flags);
                    continue;
                }
                world.setBlock(add, Blocks.AIR.defaultBlockState(), flags);
            }
        }
        for (Template.BlockInfo block : blocks.values()) {
            BlockPos add = block.pos.offset(anchor);
//			if (!shouldUpdateAfterMovement(block))
//				continue;

            int flags = Constants.BlockFlags.IS_MOVING | Constants.BlockFlags.DEFAULT;
            world.sendBlockUpdated(add, block.state, Blocks.AIR.defaultBlockState(), flags);

            // when the blockstate is set to air, the block's POI data is removed, but markAndNotifyBlock tries to
            // remove it again, so to prevent an error from being logged by double-removal we add the POI data back now
            // (code copied from ServerWorld.onBlockStateChange)
            ServerWorld serverWorld = (ServerWorld) world;
            PointOfInterestType.forState(block.state)
                    .ifPresent(poiType -> {
                        world.getServer()
                                .execute(() -> {
                                    serverWorld.getPoiManager()
                                            .add(add, poiType);
                                    DebugPacketSender.sendPoiAddedPacket(serverWorld, add);
                                });
                    });

            world.markAndNotifyBlock(add, world.getChunkAt(add), block.state, Blocks.AIR.defaultBlockState(), flags,
                    512);
            block.state.updateIndirectNeighbourShapes(world, add, flags & -2);
        }
    }
    @OnlyIn(Dist.CLIENT)
    protected void invalidate(Contraption contraption) {
        ContraptionRenderDispatcher.invalidate(contraption);
    }
    static final int PlotWidth=128;
    static final int PlotCenterHeight=64;
    public static BlockPos getPlotPosFromId(int id)
    {
        return new BlockPos(64,64,64);
    }
    public static int getIdFromPlotPos(BlockPos pos)
    {
        return 0;
    }
    public int getNextId()
    {
        return 0;
    }
    public static class AirshipEventHandler
    {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void tickEvent(TickEvent.ServerTickEvent e)
        {
            if(e.phase == TickEvent.Phase.START)
            {
                INSTANCE.tick();
            }
        }
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void renderStartEvent(TickEvent.RenderTickEvent e)
        {
            //if(e.phase == TickEvent.Phase.START)
            //{
            //    for (Map.Entry<Integer,AirshipContraptionEntity> entry: INSTANCE.AllAirships.entrySet()) {
            //        ServerWorld world = AirshipDimensionManager.INSTANCE.getWorld();
            //        BlockPos anchorPos = getPlotPosFromId(entry.getKey());
            //        AirshipContraptionEntity entity = entry.getValue();
            //        AirshipContraptionData data = INSTANCE.AirshipData.get(entry.getKey());
            //        if(entity.level.isLoaded(entity.blockPosition())) {
            //            for (Map.Entry<BlockPos, ControlledContraptionEntity> entry2 : data.controlledContraptions.entrySet()) {
            //                ControlledContraptionEntity contraptionEntity=entry2.getValue();
            //                //((AbstractContraptionEntityExtension)contraptionEntity).createAeronautics$setOriginalPosition(contraptionEntity.position());
            //                Vector3d v = entity.position().add(contraptionEntity.position()).subtract(new Vector3d(anchorPos.getX(),anchorPos.getY(),anchorPos.getZ()));
            //                contraptionEntity.setPos(v.x,v.y,v.z);
            //            }
            //        }
            //    }
            //}
        }
    }
}
