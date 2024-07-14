package com.eriksonn.createaeronautics.physics.collision.shape;

import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshCollisionShapeGenerator implements ICollisionShapeGenerator {

    AbstractContraptionRigidbody rigidbody;

    public MeshCollisionShapeGenerator(AbstractContraptionRigidbody rigidbody) {
        this.rigidbody = rigidbody;
    }

    /**
     * Computes the vertices of a given AABB
     */
    public static Vector3d[] vertices(AxisAlignedBB box) {
        return new Vector3d[]{
                new Vector3d(box.minX, box.minY, box.minZ),
                new Vector3d(box.maxX, box.minY, box.minZ),
                new Vector3d(box.maxX, box.minY, box.maxZ),
                new Vector3d(box.minX, box.minY, box.maxZ),

                new Vector3d(box.minX, box.maxY, box.minZ),
                new Vector3d(box.maxX, box.maxY, box.minZ),
                new Vector3d(box.maxX, box.maxY, box.maxZ),
                new Vector3d(box.minX, box.maxY, box.maxZ)
        };
    }

    @Override
    public HashMap<BlockPos, List<ICollisionShape>> generateShapes(Contraption contraption) {
        // for every block
        Map<BlockPos, Template.BlockInfo> blocks = contraption.getBlocks();

        HashMap<BlockPos, List<ICollisionShape>> shapes = new HashMap<>();

        for (Map.Entry<BlockPos, Template.BlockInfo> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState block = entry.getValue().state;

            if(
                    blocks.containsKey(pos.above()) && blocks.get(pos.above()).state.getBlock().defaultBlockState() == Blocks.AIR.defaultBlockState()
                            && blocks.containsKey(pos.below()) && blocks.get(pos.below()).state.getBlock().defaultBlockState() == Blocks.AIR.defaultBlockState()
                            && blocks.containsKey(pos.north()) && blocks.get(pos.north()).state.getBlock().defaultBlockState() == Blocks.AIR.defaultBlockState()
                            && blocks.containsKey(pos.south()) && blocks.get(pos.south()).state.getBlock().defaultBlockState() == Blocks.AIR.defaultBlockState()
                            && blocks.containsKey(pos.east()) && blocks.get(pos.east()).state.getBlock().defaultBlockState() == Blocks.AIR.defaultBlockState()
                            && blocks.containsKey(pos.west()) && blocks.get(pos.west()).state.getBlock().defaultBlockState() == Blocks.AIR.defaultBlockState()
            ) {
                continue;
            }
            
            // generate collision shapes
            List<ICollisionShape> collisionShapes = generateFromBlock(pos, block, true);
            shapes.put(pos, collisionShapes);
        }

        return shapes;
    }

    public List<ICollisionShape> generateFromBlock(BlockPos position, BlockState block, boolean isOnContraption) {
        VoxelShape voxelShape = isOnContraption ? rigidbody.adapter.getCollisionShapeOnContraption(position) : rigidbody.adapter.getCollisionShape(position);

        List<ICollisionShape> shapes = new ArrayList<>();

        voxelShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vector3d[] vertices = vertices(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));

            // mesh collision shape
            MeshCollisionShape shape = new MeshCollisionShape(vertices);
            if(isOnContraption)
                shape.setTransform(new Vector3d(position.getX(), position.getY(), position.getZ()), rigidbody);
            else
                shape.setTransform(new Vector3d(position.getX(), position.getY(), position.getZ()), null);
            shapes.add(shape);
        });

        return shapes;
    }

}
