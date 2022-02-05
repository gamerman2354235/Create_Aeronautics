package com.eriksonn.createaeronautics.blocks.airship_assembler;

import com.eriksonn.createaeronautics.index.AllShapes;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class AirshipAssemblerBlock extends Block implements ITE<AirshipAssemblerTileEntity> {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public AirshipAssemblerBlock(AbstractBlock.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
        super.createBlockStateDefinition(builder);
    }
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return CATileEntities.AIRSHIP_ASSEMBLER.create();

    }
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context)
    {
        return AllShapes.AIRSHIP_ASSEMBLER.get(Direction.UP);

    }
    @Override
    public Class<AirshipAssemblerTileEntity> getTileEntityClass() {
        return AirshipAssemblerTileEntity.class;
    }
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!player.mayBuild()) {
            return ActionResultType.FAIL;
        } else if (player.isShiftKeyDown()) {
            return ActionResultType.FAIL;
        } else if (player.getItemInHand(handIn).isEmpty()) {
            if (worldIn.isClientSide) {
                return ActionResultType.SUCCESS;
            } else {
                this.withTileEntityDo(worldIn, pos, (te) -> {
                    boolean previouslyPowered = state.getValue(ACTIVE);
                    if (previouslyPowered == te.running)
                        worldIn.setBlock(pos, state.cycle(ACTIVE), 2);
                    if (te.running) {
                        te.disassemble();
                    } else {

                        te.assembleNextTick = true;
                    }

                });
                return ActionResultType.SUCCESS;
            }
        } else {
            return ActionResultType.PASS;
        }
    }
}
