package com.eriksonn.createaeronautics.index;

import com.simibubi.create.foundation.utility.VoxelShaper;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.function.BiFunction;

import static net.minecraft.util.Direction.UP;

public class AllShapes {

    public static final VoxelShaper
    STATIONARY_CANNON = shape(0, 0, 0, 16, 12, 16)
            .add(3, 12, 3, 13, 14, 13)
            .add(5, 14, 5, 11, 25, 11)
            .add(4, 25, 4, 12, 27, 12)
            .erase(6, 14, 6, 10, 27, 10)
            .forDirectional(),
    TORSION_SPRING = shape(0, 0, 0, 16, 3, 16)
            .add(4, 3, 4, 12, 13, 12)
            .add(2, 5, 2, 14, 11, 14)
            .add(0,13,0,16,16,16)
            .forAxis(),
    AIRSHIP_ASSEMBLER=  shape(0, 0, 0, 16, 4, 16)
            .add(2, 4, 2, 14, 12, 14)
            .add(4, 12, 4, 12, 13, 12)
            .add(6, 13, 6,10, 16, 10)
            .forAxis();
    private static AllShapes.Builder shape(VoxelShape shape) {
        return new AllShapes.Builder(shape);
    }

    private static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    public static class Builder {

        private VoxelShape shape;

        public Builder(VoxelShape shape) {
            this.shape = shape;
        }

        public AllShapes.Builder add(VoxelShape shape) {
            this.shape = VoxelShapes.or(this.shape, shape);
            return this;
        }

        public AllShapes.Builder add(double x1, double y1, double z1, double x2, double y2, double z2) {
            return add(cuboid(x1, y1, z1, x2, y2, z2));
        }

        public AllShapes.Builder erase(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.shape =
                    VoxelShapes.join(shape, cuboid(x1, y1, z1, x2, y2, z2), IBooleanFunction.ONLY_FIRST);
            return this;
        }

        public VoxelShape build() {
            return shape;
        }

        public VoxelShaper build(BiFunction<VoxelShape, Direction, VoxelShaper> factory, Direction direction) {
            return factory.apply(shape, direction);
        }

        public VoxelShaper build(BiFunction<VoxelShape, Direction.Axis, VoxelShaper> factory, Direction.Axis axis) {
            return factory.apply(shape, axis);
        }

        public VoxelShaper forDirectional(Direction direction) {
            return build(VoxelShaper::forDirectional, direction);
        }

        public VoxelShaper forAxis() {
            return build(VoxelShaper::forAxis, Direction.Axis.Y);
        }

        public VoxelShaper forHorizontalAxis() {
            return build(VoxelShaper::forHorizontalAxis, Direction.Axis.Z);
        }

        public VoxelShaper forHorizontal(Direction direction) {
            return build(VoxelShaper::forHorizontal, direction);
        }

        public VoxelShaper forDirectional() {
            return forDirectional(UP);
        }

    }
}
