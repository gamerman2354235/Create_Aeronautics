package com.eriksonn.createaeronautics.blocks.torsion_spring;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public class SpringBehaviour extends TileEntityBehaviour
{
    ValueBoxTransform firstSlot;
    ValueBoxTransform secondSlot;
    public BehaviourType<SpringBehaviour> TYPE = new BehaviourType<>();
    public SpringBehaviour(SmartTileEntity te, Pair<ValueBoxTransform, ValueBoxTransform> slots) {
        super(te);

    }
    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public static class SlotPositioning {
        Function<BlockState, Pair<Vector3d, Vector3d>> offsets;
        Function<BlockState, Vector3d> rotation;
        float scale;

        public SlotPositioning(Function<BlockState, Pair<Vector3d, Vector3d>> offsetsForState,
                               Function<BlockState, Vector3d> rotationForState) {
            offsets = offsetsForState;
            rotation = rotationForState;
            scale = 1;
        }

        public SpringBehaviour.SlotPositioning scale(float scale) {
            this.scale = scale;
            return this;
        }

    }

    public boolean testHit(Boolean first, Vector3d hit) {
        BlockState state = tileEntity.getBlockState();
        Vector3d localHit = hit.subtract(Vector3d.atLowerCornerOf(tileEntity.getBlockPos()));
        return (first ? firstSlot : secondSlot).testHit(state, localHit);
    }
}
