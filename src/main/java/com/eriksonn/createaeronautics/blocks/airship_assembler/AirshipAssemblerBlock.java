package com.eriksonn.createaeronautics.blocks.airship_assembler;

import appeng.core.sync.network.ServerPacketHandler;
import com.eriksonn.createaeronautics.index.CAShapes;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.eriksonn.createaeronautics.inspect.InspectUI;
import com.eriksonn.createaeronautics.network.NetworkMain;
import com.eriksonn.createaeronautics.network.packet.InspectAirshipPacket;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.ponder.PonderUI;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;

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
        return CAShapes.AIRSHIP_ASSEMBLER.get(Direction.UP);
    }

    @Override
    public Class<AirshipAssemblerTileEntity> getTileEntityClass() {
        return AirshipAssemblerTileEntity.class;
    }



    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {

        if (!player.mayBuild()) {
            return ActionResultType.FAIL;
        } else if (player.isShiftKeyDown()) {

            this.withTileEntityDo(worldIn, pos, (te) -> {
                if (te.running) {
                    NetworkMain.sendToPlayer((ServerPlayerEntity) player, new InspectAirshipPacket(te.movedContraption.plotId));
                }
            });
            return ActionResultType.SUCCESS;
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
