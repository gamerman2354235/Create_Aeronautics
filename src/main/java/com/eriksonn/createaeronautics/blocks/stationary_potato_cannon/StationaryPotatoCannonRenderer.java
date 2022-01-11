package com.eriksonn.createaeronautics.blocks.stationary_potato_cannon;

import com.eriksonn.createaeronautics.index.CABlockPartials;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

public class StationaryPotatoCannonRenderer extends SafeTileEntityRenderer<StationaryPotatoCannonTileEntity> {
    public StationaryPotatoCannonRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }
    Direction facing;
    protected void renderSafe(StationaryPotatoCannonTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
        //this.renderItem(te, partialTicks, ms, buffer, light, overlay);
        this.facing = (Direction)te.getBlockState().getValue(DirectionalKineticBlock.FACING);
        FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);
        if (!Backend.getInstance().canUseInstancing(te.getLevel())) {
            this.renderComponents(te, partialTicks, ms, buffer, light, overlay);
        }
        renderItem(te,partialTicks,ms,buffer,light,overlay);
    }
    protected void renderComponents(StationaryPotatoCannonTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
        IVertexBuilder vb = buffer.getBuffer(RenderType.solid());
        if (!Backend.getInstance().canUseInstancing(te.getLevel())) {
            KineticTileEntityRenderer.renderRotatingKineticBlock(te, this.getRenderedBlockState(te), ms, vb, light);
        }

        BlockState blockState = te.getBlockState();
        BlockPos pos = te.getBlockPos();
        //Vector3d offset = this.getHandOffset(te, partialTicks, blockState);
        Vector3d offset = new Vector3d(0,0,0);
        SuperByteBuffer barrel = PartialBufferer.get(CABlockPartials.CANNON_BARREL,blockState);
        //SuperByteBuffer pole = PartialBufferer.get(AllBlockPartials.DEPLOYER_POLE, blockState);
        //SuperByteBuffer hand = PartialBufferer.get(te.getHandPose(), blockState);
        transform(barrel.translate(offset.x, offset.y, offset.z), blockState, true).renderInto(ms, vb);
        //transform(te.getLevel(), pole.translate(offset.x, offset.y, offset.z), blockState, pos, true).renderInto(ms, vb);
        //transform(te.getLevel(), hand.translate(offset.x, offset.y, offset.z), blockState, pos, false).renderInto(ms, vb);
    }
    private static SuperByteBuffer transform(SuperByteBuffer buffer, BlockState deployerState, boolean axisDirectionMatters) {
        Direction facing = deployerState.getValue(FACING);

        float zRotLast =
                axisDirectionMatters && (deployerState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) ? 90
                        : 0;
        float yRot = AngleHelper.horizontalAngle(facing);
        float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

        buffer.rotateCentered(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
        buffer.rotateCentered(Direction.UP, (float) ((yRot) / 180 * Math.PI));
        buffer.rotateCentered(Direction.SOUTH, (float) ((zRotLast) / 180 * Math.PI));
        return buffer;
    }
    protected BlockState getRenderedBlockState(KineticTileEntity te) {
        return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
    }
    public void renderItem(StationaryPotatoCannonTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
                                  int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance()
                .getItemRenderer();
        MatrixTransformStack msr = MatrixTransformStack.of(ms);
        ms.pushPose();
        msr.centre();
        Vector3i facingVec = facing.getNormal();
        float itemScale = .3f;
        float normalizedTimer= (te.ItemTimer + partialTicks);
        float itemPosition = 1-(float)Math.exp(-0.25f*normalizedTimer);
        itemPosition*=0.7f;
        ms.translate(facingVec.getX()*itemPosition, facingVec.getY()*itemPosition, facingVec.getZ()*itemPosition);
        ms.scale(itemScale, itemScale, itemScale);
        Quaternion Q = new Quaternion((float)Math.sin(te.ItemRotationId*0.4f),(float)Math.cos(te.ItemRotationId*1.4f),(float)Math.sin(te.ItemRotationId*3.0f),(float)Math.cos(te.ItemRotationId*5.0f));
        Q.normalize();
        msr.multiply(Q);
        //msr.rotateX(itemPosition * 180);
        //msr.rotateY(itemPosition * 180);
        itemRenderer.renderStatic(te.currentStack, ItemCameraTransforms.TransformType.FIXED, light, overlay, ms, buffer);
        ms.popPose();
    }
}
