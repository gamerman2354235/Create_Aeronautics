package com.eriksonn.createaeronautics.contraptions;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionInteractionPacket;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableObject;

@Mod.EventBusSubscriber
public class AirshipContraptionHandlerClient {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void rightClickingOnContraptionsGetsHandledLocally(InputEvent.ClickInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null)
            return;
        //if (player.isPassenger())
        //    return;
        if (mc.level == null)
            return;
        if (!event.isUseItem())
            return;
        Vector3d origin = RaycastHelper.getTraceOrigin(player);

        double reach = mc.gameMode.getPickRange();
        if (mc.hitResult != null && mc.hitResult.getLocation() != null)
            reach = Math.min(mc.hitResult.getLocation()
                    .distanceTo(origin), reach);

        Vector3d target = RaycastHelper.getTraceTarget(player, reach, origin);
        for (AirshipContraptionEntity contraptionEntity : mc.level
                .getEntitiesOfClass(AirshipContraptionEntity.class, new AxisAlignedBB(origin, target))) {

            Vector3d localOrigin = contraptionEntity.toLocalVector(origin, 1);
            Vector3d localTarget = contraptionEntity.toLocalVector(target, 1);
            Contraption contraption = contraptionEntity.getContraption();

            MutableObject<BlockRayTraceResult> mutableResult = new MutableObject<>();
            RaycastHelper.PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(localOrigin, localTarget, p -> {
                Template.BlockInfo blockInfo = contraption.getBlocks()
                        .get(p);
                if (blockInfo == null)
                    return false;
                BlockState state = blockInfo.state;
                VoxelShape raytraceShape = state.getShape(Minecraft.getInstance().level, BlockPos.ZERO.below());
                if (raytraceShape.isEmpty())
                    return false;
                BlockRayTraceResult rayTrace = raytraceShape.clip(localOrigin, localTarget, p);
                if (rayTrace != null) {
                    mutableResult.setValue(rayTrace);
                    return true;
                }
                return false;
            });

            if (predicateResult == null || predicateResult.missed())
                return;

            BlockRayTraceResult rayTraceResult = mutableResult.getValue();
            Hand hand = event.getHand();
            Direction face = rayTraceResult.getDirection();
            BlockPos pos = rayTraceResult.getBlockPos();

            if (!contraptionEntity.handlePlayerInteraction2(player, pos, face, hand))
                return;
            AllPackets.channel.sendToServer(new ContraptionInteractionPacket(contraptionEntity, hand, pos, face));
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }
}
