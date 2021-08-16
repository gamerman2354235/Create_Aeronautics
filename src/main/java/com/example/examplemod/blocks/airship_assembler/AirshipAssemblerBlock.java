package com.example.examplemod.blocks.airship_assembler;

import com.example.examplemod.blocks.spring_loaded_gearshift.SpringLoadedGearshiftTileEntity;
import com.example.examplemod.index.CATileEntities;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class AirshipAssemblerBlock extends Block implements ITE<AirshipAssemblerTileEntity> {
    public AirshipAssemblerBlock(AbstractBlock.Properties properties) {
        super(properties);
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
