package com.eriksonn.createaeronautics.events;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommonEvents {
    @SubscribeEvent
    public static void onEntityAdded(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        World world = event.getWorld();
        if(entity instanceof ControlledContraptionEntity && world.dimension() == AirshipDimensionManager.WORLD_ID && !world.isClientSide) {
            ControlledContraptionEntity contraptionEntity=(ControlledContraptionEntity)entity;
            int PlotId = AirshipManager.getIdFromPlotPos(contraptionEntity.blockPosition());
            AirshipContraptionEntity airshipEntity = AirshipManager.INSTANCE.AllAirships.get(PlotId);
            if(airshipEntity!=null)
                ContraptionHandler.addSpawnedContraptionsToCollisionList(entity, airshipEntity.level);
        }
    }
}
