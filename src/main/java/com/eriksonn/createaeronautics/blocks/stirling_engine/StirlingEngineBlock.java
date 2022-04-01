package com.eriksonn.createaeronautics.blocks.stirling_engine;


import com.eriksonn.createaeronautics.blocks.propeller_bearing.PropellerBearingBlock;
import com.eriksonn.createaeronautics.index.CAShapes;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.Consumer;

import static net.minecraft.block.AbstractFurnaceBlock.LIT;

public class StirlingEngineBlock extends HorizontalKineticBlock {
    public StirlingEngineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return CATileEntities.STIRLING_ENGINE.create();

    }
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT);
        super.createBlockStateDefinition(builder);
    }
    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
            StirlingEngineTileEntity te = (StirlingEngineTileEntity) worldIn.getBlockEntity(pos);
            if (te !=null&&!te.currentStack.isEmpty())
                InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), te.currentStack);
            worldIn.removeBlockEntity(pos);
        }
    }
    @Override
    public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING);
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred == null || (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())) {
            Direction horizontalDirection = context.getHorizontalDirection();
            return defaultBlockState().setValue(HORIZONTAL_FACING, (context.getPlayer() != null && context.getPlayer()
                    .isShiftKeyDown()) ? horizontalDirection.getOpposite():horizontalDirection);
        }
        return defaultBlockState().setValue(HORIZONTAL_FACING, preferred);

    }
    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        ItemStack heldByPlayer = player.getItemInHand(handIn)
                .copy();
        if (AllItems.WRENCH.isIn(heldByPlayer))
            return ActionResultType.PASS;


        if (worldIn.isClientSide)
            return ActionResultType.SUCCESS;
        StirlingEngineTileEntity te = (StirlingEngineTileEntity)worldIn.getBlockEntity(pos);

            ItemStack cannonItem = te.currentStack.copy();
            if (cannonItem.isEmpty() && heldByPlayer.isEmpty())
                return ActionResultType.SUCCESS;
            if((!heldByPlayer.isEmpty() && !te.invHandler.orElse(null).isItemValid(0,heldByPlayer)))
                return ActionResultType.PASS;
            if(cannonItem.isEmpty())
            {
                te.currentStack =heldByPlayer;
                player.setItemInHand(handIn,ItemStack.EMPTY);

            }else
            if(!heldByPlayer.isEmpty())
            {
                if(!ItemHandlerHelper.canItemStacksStack(cannonItem, heldByPlayer))
                {
                    return ActionResultType.SUCCESS;
                }else
                {
                    int TargetAmount=cannonItem.getCount()+heldByPlayer.getCount();
                    int TransferAmount = Math.min(TargetAmount-cannonItem.getCount(),heldByPlayer.getCount());
                    if(TransferAmount==0)
                        return ActionResultType.SUCCESS;
                    te.currentStack.shrink(-TransferAmount);
                    heldByPlayer.shrink(TransferAmount);
                    player.setItemInHand(handIn,heldByPlayer);
                }
            }else
            {
                te.currentStack=ItemStack.EMPTY;
                player.setItemInHand(handIn,cannonItem);
            }

            te.sendData();

        return ActionResultType.SUCCESS;
    }
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context)
    {
        return CAShapes.STIRLING_ENGINE.get(state.getValue(HORIZONTAL_FACING));

    }

    public static boolean isLitState(BlockState blockState) {
        return blockState.getValue(LIT);
    }
}
