package com.eriksonn.createaeronautics.inspect;

import com.eriksonn.createaeronautics.physics.SimulatedContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.api.PhysicsAdapter;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

public class PonderPhysicsAdapter implements PhysicsAdapter {
    InspectUI inspectUI;

    public PonderPhysicsAdapter(InspectUI inspectUI) {
        this.inspectUI = inspectUI;
    }

    @Override
    public Vector3d position() {
        return inspectUI.sectionPosition;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return inspectUI.scene.getWorld().getBlockState(pos).getFluidState();
    }

    @Override
    public Vector3d toGlobalVector(Vector3d localVec, float partialTicks) {
        double x = /*MathHelper.lerp(partialTicks, oldPosition.x, */position().x;
        double y = /*MathHelper.lerp(partialTicks, oldPosition.y, */position().y;
        double z = /*MathHelper.lerp(partialTicks, oldPosition.z, */position().z;
        Vector3d anchorVec = new Vector3d(x, y, z);

        Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
        localVec = localVec.subtract(rotationOffset).subtract(inspectUI.rigidbody.getCenterOfMass());
        localVec = applyRotation(localVec, partialTicks);
        localVec = localVec.add(rotationOffset)
                .add(anchorVec);
        return localVec;
    }

    @Override
    public Vector3d applyRotation(Vector3d localPos, float partialTicks) {
        return SimulatedContraptionRigidbody.rotateQuat(localPos, inspectUI.rigidbody.getPartialOrientation(partialTicks));
    }

    @Override
    public Vector3d getPlotOffset() {
        return new Vector3d(0, -128, 0);
    }

    @Override
    public BlockState getBlockState(BlockPos worldPos) {
        return inspectUI.scene.getWorld().getBlockState(worldPos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockPos position) {
        if(position.getY() > inspectUI.basePlateTop) {
            return VoxelShapes.empty();
        }
        return inspectUI.scene.getWorld().getBlockState(position).getCollisionShape(inspectUI.scene.getWorld(), position);
    }

    @Override
    public VoxelShape getCollisionShapeOnContraption(BlockPos position) {
        return inspectUI.airship.getContraption().getBlocks().get(position).state.getCollisionShape(inspectUI.airship.getContraption().getContraptionWorld(), position);
//        return inspectUI.scene.getWorld().getBlockState();
    }
}
