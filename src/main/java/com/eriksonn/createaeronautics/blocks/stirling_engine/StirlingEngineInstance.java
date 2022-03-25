package com.eriksonn.createaeronautics.blocks.stirling_engine;

import com.eriksonn.createaeronautics.index.CABlockPartials;
import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.InstanceMaterial;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;


public class StirlingEngineInstance extends KineticTileInstance<StirlingEngineTileEntity> implements IDynamicInstance {

    protected final Direction facing;
    protected final RotatingData shaft;
    protected ModelData[] pistons=new ModelData[4];
    public StirlingEngineInstance(MaterialManager<?> modelManager, StirlingEngineTileEntity tile) {
        super(modelManager, tile);

        facing = blockState.getValue(HORIZONTAL_FACING);
        shaft = setup(shaftModel().createInstance());
        for (int i =0;i<4;i++)
        {
            InstanceMaterial<ModelData> mat = getTransformMaterial();
            pistons[i]=mat.getModel(CABlockPartials.ENGINE_PISTON, blockState).createInstance();
        }
    }

    @Override
    public void beginFrame() {
        float partialTicks = AnimationTickHolder.getPartialTicks();
        float speed = tile.visualSpeed.get(partialTicks) * 3 / 10f;
        float angle = tile.angle + speed * partialTicks;

        animate(angle);
    }
    private void animate(float angle) {
        MatrixStack ms = new MatrixStack();
        MatrixTransformStack msr = MatrixTransformStack.of(ms);

        msr.translate(getInstancePosition());

        float rotation =
                facing .getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -angle
                        : angle;
        ms.pushPose();
        rotateToFacing(msr,facing);
        for (int i =0;i<4;i++) {
            ms.pushPose();
            int y = i/2;
            int x = i%2;

            double shift = Math.sin((rotation- i*90+45) * Math.PI / 180.0)*1.5f;

            msr.centre();
            msr.rotate(Direction.SOUTH,(float)(Math.PI*0.25 * (x*2-1)));
            msr.translate(0,1.5/16.0,0);
            msr.unCentre();
            msr.translate(0,(6.5 + shift) / 16.0,(7.5+y*4)/16.0);

            pistons[i].setTransform(ms);
            ms.popPose();
        }
        ms.popPose();
        msr.centre()
                .rotate(Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()), AngleHelper.rad(angle))
                .unCentre();
    }

    @Override
    public void remove() {
        shaft.delete();
        for (int i =0;i<4;i++)
        {
            pistons[i].delete();
        }
    }
    @Override
    public void update() {
        updateRotation(shaft);
    }

    @Override
    public void updateLight() {
        relight(pos, shaft);
        relight(pos,pistons);
        //if (connection != null) {
        //    relight(this.pos.relative(connection), connectors.stream());
        //}
    }
    protected Instancer<RotatingData> shaftModel() {
        Direction opposite = facing;
        return getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, blockState, opposite);
    }
    protected void rotateToFacing(MatrixTransformStack buffer, Direction facing) {
        buffer.centre()
                .rotate(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
                .unCentre();
    }
}
