package com.eriksonn.createaeronautics.blocks.stationary_potato_cannon;


import com.eriksonn.createaeronautics.index.CAShapes;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class StationaryPotatoCannonBlock extends DirectionalAxisKineticBlock implements ITE<StationaryPotatoCannonTileEntity> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public StationaryPotatoCannonBlock(Properties properties) {
        super(properties);
    }
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return CATileEntities.STATIONARY_POTATO_CANNON.create();
    }

    public Class<StationaryPotatoCannonTileEntity> getTileEntityClass() {
        return StationaryPotatoCannonTileEntity.class;
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(POWERED,
                context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        if (worldIn.isClientSide)
            return;
        this.withTileEntityDo(worldIn, pos, StationaryPotatoCannonTileEntity::updateSignal);
        boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered != worldIn.hasNeighborSignal(pos)) {
            worldIn.setBlock(pos, state.cycle(POWERED), 2);
        }
    }
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context)
    {
        return CAShapes.STATIONARY_CANNON.get(state.getValue(FACING));

    }
    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        ItemStack heldByPlayer = player.getItemInHand(handIn)
                .copy();
        if (AllItems.WRENCH.isIn(heldByPlayer))
            return ActionResultType.PASS;

        if (hit.getDirection() != state.getValue(FACING))
            return ActionResultType.PASS;
        if (worldIn.isClientSide)
            return ActionResultType.SUCCESS;

        withTileEntityDo(worldIn, pos, te -> {
            ItemStack cannonItem = te.currentStack.copy();
            if (cannonItem.isEmpty() && heldByPlayer.isEmpty())
                return;
            if((!heldByPlayer.isEmpty() && !te.invHandler.orElse(null).isItemValid(0,heldByPlayer)))
                return;
            if(cannonItem.isEmpty())
            {
                int TransferAmount=Math.min(heldByPlayer.getCount(),16);
                ItemStack remainder = heldByPlayer.copy();
                ItemStack split = remainder.split(TransferAmount);
                te.currentStack =split;
                player.setItemInHand(handIn,remainder);
                te.ItemTimer = 0;
                te.RandomizeItemRotation();

            }else
            if(!heldByPlayer.isEmpty())
            {
                if(!ItemHandlerHelper.canItemStacksStack(cannonItem, heldByPlayer))
                {
                    return;
                }else
                {
                    int TargetAmount=Math.min(cannonItem.getCount()+heldByPlayer.getCount(),16);
                    int TransferAmount = Math.min(TargetAmount-cannonItem.getCount(),heldByPlayer.getCount());
                    if(TransferAmount==0)
                        return;
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
        });

        return ActionResultType.SUCCESS;
    }
    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof ItemEntity)
            return;

        withTileEntityDo(worldIn, pos, te -> {
            Vector3d hitboxPos = te.getAimingVector().scale(0.25);

            if (!new AxisAlignedBB(pos).deflate(6/8.0).move(hitboxPos).intersects(entityIn.getBoundingBox()))
                return;

        });

    }
}
