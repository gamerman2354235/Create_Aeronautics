package com.eriksonn.createaeronautics.blocks.stirling_engine;


import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class StirlingEngineBlock extends HorizontalKineticBlock {
    public StirlingEngineBlock(Properties properties) {
        super(properties);
    }
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return CATileEntities.STIRLING_ENGINE.create();
    }
    @Override
    public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING).getOpposite();
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferred.getOpposite());
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
    }
}
