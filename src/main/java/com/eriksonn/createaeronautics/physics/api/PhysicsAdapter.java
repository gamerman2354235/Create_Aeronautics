package com.eriksonn.createaeronautics.physics.api;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

public interface PhysicsAdapter {

    Vector3d position();

    FluidState getFluidState(BlockPos pos);

    Vector3d toGlobalVector(Vector3d add, float v);

    Vector3d applyRotation(Vector3d facingVector, float v);

    Vector3d getPlotOffset();

    BlockState getBlockState(BlockPos worldPos);

    VoxelShape getCollisionShape(BlockPos position);

    VoxelShape getCollisionShapeOnContraption(BlockPos position);
}
