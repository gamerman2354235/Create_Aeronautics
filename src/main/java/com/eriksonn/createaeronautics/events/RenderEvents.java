package com.eriksonn.createaeronautics.events;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.concurrent.atomic.AtomicReference;

public class RenderEvents {

    /**
     * Renders block outline for contraptions
     */
    /*public static void drawLine(VertexConsumer consumer, Matrix4f matrix, Vector3d vecA, Vector3d vecB, Vector3d normal, int r, int g, int b, int a)
    {
        consumer.vertex(matrix, (float)vecA.x, (float)vecA.y, (float)vecA.z).color(r, g, b, a).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        consumer.vertex(matrix, (float)vecB.x, (float)vecB.y, (float)vecB.z).color(r, g, b, a).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }*/

    @SubscribeEvent
    public static void renderList(RenderWorldLastEvent event) {


        Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push("renderVehicleDebug");

        ClientPlayerEntity player = mc.player;
        if(player == null) return;

        Vector3d origin = RaycastHelper.getTraceOrigin(mc.player);

        double reach = mc.gameMode.getPickRange();
        if (mc.hitResult != null && mc.hitResult.getLocation() != null)
            reach = Math.min(mc.hitResult.getLocation()
                    .distanceTo(origin), reach);

        Vector3d target = RaycastHelper.getTraceTarget(mc.player, reach, origin);
        for (AirshipContraptionEntity contraptionEntity : mc.level
                .getEntitiesOfClass(AirshipContraptionEntity.class, new AxisAlignedBB(origin, target))) {

            Vector3d localOrigin = contraptionEntity.toLocalVector(origin, 1);
            Vector3d localTarget = contraptionEntity.toLocalVector(target, 1);
            Contraption contraption = contraptionEntity.getContraption();

            MutableObject<BlockRayTraceResult> mutableResult = new MutableObject<>();

            AtomicReference<VoxelShape> voxelShape = new AtomicReference();
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
                    voxelShape.set(raytraceShape);
                    return true;
                }
                return false;
            });

            if (predicateResult == null || predicateResult.missed())
                return;

            BlockRayTraceResult rayTraceResult = mutableResult.getValue();
            BlockPos blockPos = rayTraceResult.getBlockPos();

            renderBlockOutline(contraptionEntity, event.getMatrixStack(), voxelShape.get(), blockPos, event.getPartialTicks());
        }
        mc.getProfiler().pop();

    }

    public static void renderBlockOutline(AirshipContraptionEntity airship, MatrixStack stack, VoxelShape shape, BlockPos blockPos, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        stack.pushPose();

        Vector3d playerPos = mc.gameRenderer.getMainCamera().getPosition();
        stack.translate(-playerPos.x, -playerPos.y, -playerPos.z);

        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
        IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
        Matrix4f matrix = stack.last().pose();

        shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {

            // The points of the triangle
            Vector3d vecA = new Vector3d(minX, minY, minZ).add(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            vecA = airship.toGlobalVector(vecA, partialTicks);
            Vector3d vecB = new Vector3d(maxX, maxY, maxZ).add(new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            vecB = airship.toGlobalVector(vecB, partialTicks);
            int r = 0, g = 0, b = 0, a = (int) (0.4 * 255.0);

            double xDiff = (maxX - minX);
            double yDiff = (maxY - minY);
            double zDiff = (maxZ - minZ);

            double magnitude = Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
            xDiff /= magnitude;
            yDiff /= magnitude;
            zDiff /= magnitude;

            Matrix3f lastNormal = stack.last().normal();
            builder.vertex(matrix, (float)vecA.x, (float)vecA.y, (float)vecA.z).color(r, g, b, a).normal(lastNormal, (float) xDiff, (float) yDiff, (float) zDiff).endVertex();
            builder.vertex(matrix, (float)vecB.x, (float)vecB.y, (float)vecB.z).color(r, g, b, a).normal(lastNormal, (float) xDiff, (float) yDiff, (float) zDiff).endVertex();
        });

        buffer.endBatch();
        stack.popPose();
    }

}
