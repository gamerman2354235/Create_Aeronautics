package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipContraption;
import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContraptionRenderDispatcher.class)
public class ContraptionRenderMixin {

    /**
     * @author Eriksonn
     */
    @Overwrite(remap=false)
    public static void renderTileEntities(World world, PlacementSimulationWorld renderWorld, Contraption c, ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
            MatrixStack ms = matrices.getModelViewProjection();

            if(c instanceof AirshipContraption) {
                int plotId=((AirshipContraptionEntity)c.entity).plotId;
                BlockPos anchorPos = AirshipManager.getPlotPosFromId(plotId);
                ms.translate(-anchorPos.getX(),-anchorPos.getY(),-anchorPos.getZ());
            }
            //if(world.dimension() == AirshipDimensionManager.WORLD_ID)
            //{
            //    int plotId = AirshipManager.getIdFromPlotPos(c.anchor);
            //    AirshipContraptionEntity entity = AirshipManager.INSTANCE.AllAirships.get(plotId);
            //}
        TileEntityRenderHelper.renderTileEntities(world, renderWorld, c.specialRenderedTileEntities,
                ms, matrices.getLight(), buffer);
    }
}
