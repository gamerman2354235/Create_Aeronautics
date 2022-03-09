package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.index.CAEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.foundation.utility.UniqueLinkedList;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class AirshipContraption extends Contraption {
    public World storageWorld;
    public AirshipContraption() {

    }
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        //BlockPos offset = pos.relative(Direction.DOWN);
        BlockPos offset = pos;
        if (!this.searchMovedStructure(world, offset, (Direction)null)) {
            return false;
        } else {
            this.startMoving(world);
            this.expandBoundsAroundAxis(Direction.Axis.Y);

            return !this.blocks.isEmpty();

        }
    }
    public boolean i(World world, BlockPos pos, @Nullable Direction forcedDirection)
            throws AssemblyException {
        Queue<BlockPos> frontier = new UniqueLinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        anchor = pos;
        if (bounds == null)
            bounds = new AxisAlignedBB(BlockPos.ZERO);

        frontier.add(pos);
        moveBlock(world, forcedDirection, frontier, visited);
        pos = pos.offset(0,-1,0);


        if (!BlockMovementChecks.isBrittle(world.getBlockState(pos)))
            frontier.add(pos);
        if (!addToInitialFrontier(world, pos, forcedDirection, frontier))
            return false;
        for (int limit = 100000; limit > 0; limit--) {
            if (frontier.isEmpty())
                return true;
            if (!moveBlock(world, forcedDirection, frontier, visited))
                return false;
        }
        throw AssemblyException.structureTooLarge();
    }
    public void setBlockState(BlockPos localPos, BlockState state, TileEntity be) {
        CompoundNBT nbt = null;
        if (be != null) {
            nbt = be.serializeNBT();

            nbt.remove("x");
            nbt.remove("y");
            nbt.remove("z");
        }

        Template.BlockInfo info = blocks.get(localPos);
        blocks.put(localPos, new Template.BlockInfo(localPos, state, be == null ? null : nbt));
    }

    protected boolean isAnchoringBlockAt(BlockPos pos) {
        //return pos.equals(this.anchor.relative(this.facing.getOpposite()));
        return false;
    }
    protected ContraptionType getType() {
        return CAEntityTypes.AIRSHIP;
    }

    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        return false;
    }
    @Override
    public ContraptionLighter<?> makeLighter() {
        return new NonStationaryLighter<>(this);
    }
}
