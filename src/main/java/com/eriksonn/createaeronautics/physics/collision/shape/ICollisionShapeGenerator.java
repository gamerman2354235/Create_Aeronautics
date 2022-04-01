package com.eriksonn.createaeronautics.physics.collision.shape;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.HashMap;
import java.util.List;

/**
 * Interface for generating collision shapes from contraptions & blocks.
 *
 * @author RyanHCode
 */
public interface ICollisionShapeGenerator {

    /**
     * Generates collision shapes from a given contraption.
     * @param contraption The contraption to generate the collision shape from.
     * @return The generated collision shapes.
     */
    HashMap<BlockPos, List<ICollisionShape>> generateShapes(AbstractContraptionEntity contraption);

    /**
     * Generates collision shapes given a block
     */
    List<ICollisionShape> generateFromBlock(IBlockReader level, BlockPos position, BlockState block, boolean isOnContraption);

}
