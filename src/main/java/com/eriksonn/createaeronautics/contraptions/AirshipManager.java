package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.google.common.eventbus.Subscribe;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IGenericEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.HashMap;
import java.util.Map;

public class AirshipManager {
    public static final AirshipManager INSTANCE = new AirshipManager();

    public Map<BlockPos,AirshipContraptionEntity> AllAirships;
    public Map<BlockPos,Map<BlockPos,BlockState>> ClientBlockStateChanges;
    public Map<BlockPos,Map<BlockPos, TileEntity>> ClientTileStateChanges;
    AirshipManager()
    {
        AllAirships=new HashMap<>();
        ClientBlockStateChanges=new HashMap<>();
        ClientTileStateChanges=new HashMap<>();
        MinecraftForge.EVENT_BUS.register(AirshipEventHandler.class);
    }
    public void tryAddEntity(int index ,AirshipContraptionEntity E)
    {
        BlockPos anchorPos=getPlotPosFromId(index);
        AllAirships.putIfAbsent(anchorPos,E);
        ClientBlockStateChanges.putIfAbsent(anchorPos,new HashMap<>());
        ClientTileStateChanges.putIfAbsent(anchorPos,new HashMap<>());
    }
    public void tick()
    {

    }
    public void performClientBlockStateChanges(AirshipContraptionEntity entity)
    {
        BlockPos anchorPos=getPlotPosFromId(entity.id);
        if(ClientBlockStateChanges!=null) {
            Map<BlockPos, BlockState> currentMap = ClientBlockStateChanges.get(anchorPos);
            Map<BlockPos, TileEntity> currentMapTe = ClientTileStateChanges.get(anchorPos);
            if(currentMap!=null) {
                for (Map.Entry<BlockPos, BlockState> entry : currentMap.entrySet()) {
                    BlockPos localPos = entry.getKey();
                    BlockState state = entry.getValue();
                    if(entity.airshipContraption!=null) {
                        entity.airshipContraption.setBlockState(localPos, state);
                        if (entity.level.isClientSide)
                            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> invalidate(entity.airshipContraption));
                    }
                }
                currentMap.clear();
                for (Map.Entry<BlockPos, TileEntity> entry : currentMapTe.entrySet()) {
                    BlockPos localPos = entry.getKey();
                    TileEntity tile = entry.getValue();
                    if(entity.airshipContraption!=null) {
                        entity.airshipContraption.presentTileEntities.put(localPos,tile);
                        if (entity.level.isClientSide)
                            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> invalidate(entity.airshipContraption));
                    }
                }
                currentMapTe.clear();
            }
        }
    }
    public void blockStateChange(BlockPos pos, BlockState first, BlockState second)
    {

        TileEntity te = AirshipDimensionManager.INSTANCE.getWorld().getBlockEntity(pos);
        int id = getIdFromPlotPos(pos);
        BlockPos anchorPos = getPlotPosFromId(id);
        pos=pos.subtract(anchorPos);
        AirshipContraptionEntity entity = AllAirships.get(anchorPos);
        Map<BlockPos,BlockState> currentMap = ClientBlockStateChanges.get(anchorPos);
        Map<BlockPos,TileEntity> currentMapTe = ClientTileStateChanges.get(anchorPos);

        if(currentMap!=null) {

            if(te!=null)
                currentMapTe.put(pos,te);
            currentMap.put(pos, second);
            entity.airshipContraption.setBlockState(pos, second);
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
        return new BlockPos(PlotWidth/2,PlotCenterHeight,PlotWidth/2);
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
    }
}
