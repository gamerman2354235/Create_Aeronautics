package com.eriksonn.createaeronautics.physics.collision.detection;

import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.collision.resolution.IIterativeManifoldSolver;
import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;
import com.eriksonn.createaeronautics.physics.collision.shape.MeshCollisionShapeGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class AbstractContraptionBasedCollisionDetector implements ICollisionDetector {

    @Override
    public List<Contact> solve(AbstractContraptionRigidbody rb, List<Contact> contacts) {
// iterate over all collision shapes
        final HashMap<BlockPos, List<ICollisionShape>> collisionShapes = rb.getCollisionShapes();

        World contraptionLevel = rb.getContraption().level;

        for (Map.Entry<BlockPos, List<ICollisionShape>> shapeEntry : collisionShapes.entrySet()) {

            // check if this block is not fully surrounded
            for (ICollisionShape shape : shapeEntry.getValue()) {

                // now that we're iterating over every shape, get it's world space bounds that it occupies
                AxisAlignedBB bounds = shape.getBounds();

                // inflate it by a tiny amount to account for floating point errors
                bounds = bounds.inflate(0.1);

                // now we need to iterate over every block in the world within the bounds
                for(int x = (int)Math.floor(bounds.minX); x <= Math.ceil(bounds.maxX); x++) {
                    for(int y = (int)Math.floor(bounds.minY); y <= Math.ceil(bounds.maxY); y++) {
                        for(int z = (int)Math.floor(bounds.minZ); z <= Math.ceil(bounds.maxZ); z++) {

                            // get the block at the current position
                            BlockPos worldPos = new BlockPos(x, y, z);

                            // if the block is not air, we need to check for collisions
                            BlockState state = contraptionLevel.getBlockState(worldPos);
                            Block block = state.getBlock();
                            if(block == Blocks.AIR) continue;

                            // get the collision shape of the block
                            List<ICollisionShape> blockShape = new MeshCollisionShapeGenerator(rb).generateFromBlock(contraptionLevel, worldPos, state, false);

                            // if the block has no collision shape, we can skip it
                            if(blockShape.isEmpty()) continue;

                            // now we need to iterate over every shape in the block
                            for(ICollisionShape blockShapeShape : blockShape) {
                                // use GJKEPA to test for collisions
                                Manifold manifold = test(rb, shape, blockShapeShape, shapeEntry.getKey(), worldPos);

                                // if there's a collision, solve it
                                if(manifold != null) {
                                    contacts.add(new Contact(manifold, state, state, rb));

                                    // hi you found my super secret comment, and my debug code
                                    int a = 2 + 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        return contacts;
    }

    public abstract Manifold test(AbstractContraptionRigidbody rb, ICollisionShape shape, ICollisionShape otherShape, BlockPos localBlockPos, BlockPos worldPos);



}
