package com.eriksonn.createaeronautics.physics.api;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

public class ContraptionEntityPhysicsAdapter implements PhysicsAdapter {

    public AirshipContraptionEntity contraption;

    public ContraptionEntityPhysicsAdapter(AirshipContraptionEntity contraption) {
        this.contraption = contraption;
    }

    @Override
    public Vector3d position() {
        return contraption.position();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return contraption.level.getFluidState(pos);
    }

    @Override
    public Vector3d toGlobalVector(Vector3d add, float v) {
        return contraption.toGlobalVector(add, v);
    }

    @Override
    public Vector3d applyRotation(Vector3d facingVector, float v) {
        return contraption.applyRotation(facingVector, v);
    }

    @Override
    public Vector3d getPlotOffset() {
        BlockPos plotPos = AirshipManager.getPlotPosFromId(contraption.plotId);
        if(contraption.level.isClientSide) {
            return new Vector3d(0, plotPos.getY(), 0);
        } else {
            return new Vector3d(plotPos.getX(), plotPos.getY(), plotPos.getZ());
        }
    }

    @Override
    public BlockState getBlockState(BlockPos worldPos) {
        return contraption.level.getBlockState(worldPos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockPos position) {
        return contraption.level.getBlockState(position).getCollisionShape(contraption.level, position);
    }

    @Override
    public VoxelShape getCollisionShapeOnContraption(BlockPos position) {
        return contraption.getContraption().getBlocks().get(position).state.getCollisionShape(contraption.getContraption().getContraptionWorld(), position);
    }


}
