package com.eriksonn.createaeronautics.blocks.torsion_spring;

import com.eriksonn.createaeronautics.index.CAShapes;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.*;
import com.simibubi.create.content.contraptions.relays.encased.AbstractEncasedShaftBlock;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;


public class TorsionSpringBlock extends AbstractEncasedShaftBlock implements ITE<TorsionSpringTileEntity> {
    //public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
    //public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 5);
    public TorsionSpringBlock(Properties properties) {
        super(properties);
    }
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return CATileEntities.TORSION_SPRING.create();
    }
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {

        TileEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof KineticTileEntity) {
            KineticTileEntity kte = (KineticTileEntity)te;
            if(((TorsionSpringTileEntity) kte).outputFace!=null) {
                BlockPos InputPos = pos.offset(((TorsionSpringTileEntity) kte).outputFace.getOpposite().getNormal());
                KineticTileEntity E = (KineticTileEntity) worldIn.getBlockEntity(InputPos);
                //if (E != null) {
                //    E.network = kte.network;
                //    E.setSpeed(kte.getSpeed());
                //}

                //System.out.println("blocktick start");
                ((TorsionSpringTileEntity) kte).externalChange=false;
                RotationPropagator.handleAdded(worldIn, pos, kte);
                //System.out.println("blocktick end");
                //if (E != null) {
                //    E.network = null;
                //    E.setSpeed(0);
                //}
                //kte.setSpeed(0);
            }
        }
    }
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context)
    {
        return CAShapes.TORSION_SPRING.get(state.getValue(AXIS));

    }
    //@Override
    //protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    //    super.createBlockStateDefinition(builder.add(STATE, VERTICAL));
    //}

    /*@Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction.Axis preferredAxis = RotatedPillarKineticBlock.getPreferredAxis(context);
        if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer()
                .isCrouching()))
            return withAxis(preferredAxis, context);
        return withAxis(context.getNearestLookingDirection()
                .getAxis(), context);
    }

    @Override
    public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
        BlockState newState = state;

        if (context.getClickedFace()
                .getAxis() != Direction.Axis.Y)
            if (newState.getValue(HORIZONTAL_AXIS) != context.getClickedFace()
                    .getAxis())
                newState = newState.cycle(VERTICAL);

        return super.onWrenched(newState, context);
    }

    private BlockState withAxis(Direction.Axis axis, BlockItemUseContext context) {
        BlockState state = defaultBlockState().setValue(VERTICAL, axis.isVertical());
        if (axis.isVertical())
            return state.setValue(HORIZONTAL_AXIS, context.getHorizontalDirection()
                    .getAxis());
        return state.setValue(HORIZONTAL_AXIS, axis);
    }*/

    @Override
    public Class<TorsionSpringTileEntity> getTileEntityClass() {
        return TorsionSpringTileEntity.class;
    }
}
