package com.example.examplemod.contraptions;

import com.example.examplemod.index.CAEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.foundation.config.AllConfigs;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AirshipContraption extends Contraption {
    public World storageWorld;
    public AirshipContraption() {

    }
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        BlockPos offset = pos.relative(Direction.DOWN);
        //BlockPos offset = pos;
        if (!this.searchMovedStructure(world, offset, (Direction)null)) {
            return false;
        } else {
            this.startMoving(world);
            this.expandBoundsAroundAxis(Direction.Axis.Y);

            return !this.blocks.isEmpty();

        }
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
}
