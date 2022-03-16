package com.eriksonn.createaeronautics.blocks.propeller_bearing;

import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class PropellerBearingBlock extends BearingBlock implements ITE<PropellerBearingTileEntity> {
    public static final EnumProperty<Direction> DIRECTION = EnumProperty.create("direction",Direction.class);
    public enum Direction implements IStringSerializable {
        PUSH,PULL, ;
        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }
    public PropellerBearingBlock(Properties properties) {
        super(properties);
    }
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION);
        super.createBlockStateDefinition(builder);
    }
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return CATileEntities.PROPELLER_BEARING.create();
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        if (!player.mayBuild())
            return ActionResultType.FAIL;
        if (player.isShiftKeyDown())
            return ActionResultType.FAIL;
        if (player.getItemInHand(handIn)
                .isEmpty()) {
            if (worldIn.isClientSide) {
                withTileEntityDo(worldIn, pos, te -> {if (te.isRunning()) te.startDisassemblySlowdown();});
                return ActionResultType.SUCCESS;
            }
            withTileEntityDo(worldIn, pos, te -> {
                if (te.isRunning()) {
                    //te.disassemble();
                    te.startDisassemblySlowdown();
                    return;
                }
                te.setAssembleNextTick(true);
                //te.assembleNextTick
            });
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
    public static PropellerBearingBlock.Direction getDirectionof(BlockState blockState) {
        return blockState.hasProperty(PropellerBearingBlock.DIRECTION) ? blockState.getValue(PropellerBearingBlock.DIRECTION)
                : Direction.PULL;
    }


    @Override
    public Class<PropellerBearingTileEntity> getTileEntityClass() {
        return PropellerBearingTileEntity.class;
    }
}
