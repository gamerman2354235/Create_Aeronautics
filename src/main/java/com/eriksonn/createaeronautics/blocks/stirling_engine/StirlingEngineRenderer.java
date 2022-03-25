package com.eriksonn.createaeronautics.blocks.stirling_engine;

import com.eriksonn.createaeronautics.index.CABlockPartials;
import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class StirlingEngineRenderer extends KineticTileEntityRenderer {

    public StirlingEngineRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }@Override
    protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
                              int light, int overlay) {
        super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
        FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);
        IVertexBuilder vb = buffer.getBuffer(RenderType.solid());
        if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

        Direction direction = te.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockState blockState = te.getBlockState();

        StirlingEngineTileEntity wte = (StirlingEngineTileEntity)te;

        float speed = wte.visualSpeed.get(partialTicks) * 3 / 10f;
        float angle = wte.angle + speed * partialTicks;

        float rotation =
                direction .getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -angle
                        : angle;

        for (int i =0;i<4;i++) {

            int y = i/2;
            int x = i%2;

            double shift = Math.sin((rotation- i*90+45) * Math.PI / 180.0)*1.5f;

            SuperByteBuffer buf = PartialBufferer.get(CABlockPartials.ENGINE_PISTON, blockState);

            buf.rotateCentered(direction, (float) (Math.PI * 0.25 * (x*2-1)));
            buf.translate(rotateY(new Vector3d(0, (8.0 + shift) / 16.0, (7.5+y*4) / 16.0), direction));

            rotateToFacing(buf, direction).light(light).renderInto(ms, vb);
        }
    }
    @Override
    protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
        return PartialBufferer.getFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState(), te.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
    protected SuperByteBuffer rotateToFacing(SuperByteBuffer buffer, Direction facing) {
        buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)));
        return buffer;
    }
    Vector3d rotateY(Vector3d v,Direction facing)
    {
        Vector3i N= facing.getNormal();
        return new Vector3d(v.x*N.getZ() + v.z*N.getX(),v.y,v.z*N.getZ() - v.x*N.getX());
    }
}
