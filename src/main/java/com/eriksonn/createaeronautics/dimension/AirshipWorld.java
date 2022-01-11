package com.eriksonn.createaeronautics.dimension;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirshipWorld extends WrappedWorld implements IServerWorld {
    protected List<TileEntity> renderedTileEntities;
    protected Map<BlockPos, TileEntity> tileEntities;
    int Id;
    public AirshipWorld(World original) {

        super(original);
        this.renderedTileEntities = new ArrayList();
        this.tileEntities = new HashMap();
        Id=random.nextInt(100);
    }
    public int getBrightness(LightType p_226658_1_, BlockPos p_226658_2_) {
        return 10;
    }


    public ServerWorld getLevel() {
        if (this.world instanceof ServerWorld) {
            return (ServerWorld)this.world;
        } else {
            throw new IllegalStateException("Cannot use IServerWorld#getWorld in a client environment");
        }
    }
    public TileEntity getBlockEntity(BlockPos pos) {
        if (isOutsideBuildHeight(pos)) {
            return null;
        } else if (this.tileEntities.containsKey(pos)) {
            return (TileEntity)this.tileEntities.get(pos);
        } else {
            BlockState blockState = this.getBlockState(pos);
            if (blockState.hasTileEntity()) {
                try {
                    TileEntity tileEntity = blockState.createTileEntity(this);
                    if (tileEntity != null) {
                        this.onTEadded(tileEntity, pos);
                        this.tileEntities.put(pos, tileEntity);
                        this.renderedTileEntities.add(tileEntity);
                    }

                    return tileEntity;
                } catch (Exception var4) {
                    Create.LOGGER.debug("Could not create TE of block " + blockState + ": " + var4);
                }
            }

            return null;
        }
    }
    protected void onTEadded(TileEntity tileEntity, BlockPos pos) {
        tileEntity.setLevelAndPosition(this, pos);
    }
    public boolean destroyBlock(BlockPos arg0, boolean arg1) {
        return this.setBlock(arg0, Blocks.AIR.defaultBlockState(), 3);
    }

    public boolean removeBlock(BlockPos arg0, boolean arg1) {
        return this.setBlock(arg0, Blocks.AIR.defaultBlockState(), 3);
    }
    public boolean setBlock(BlockPos pos, BlockState arg1, int arg2) {
        //pos = pos.immutable().subtract(this.anchor);
        pos = pos.immutable();
        //this.bounds.expand(new MutableBoundingBox(pos, pos));
        //this.blocks.put(pos, arg1);
        TileEntity tileEntity;
        if (this.tileEntities.containsKey(pos)) {
            tileEntity = (TileEntity)this.tileEntities.get(pos);
            if (!tileEntity.getType().isValid(arg1.getBlock())) {
                this.tileEntities.remove(pos);
                this.renderedTileEntities.remove(tileEntity);
            }
        }

        tileEntity = this.getBlockEntity(pos);
        if (tileEntity != null) {
            this.tileEntities.put(pos, tileEntity);
        }

        return true;
    }
    @Override
    public void onBlockStateChange(BlockPos p_217393_1_, BlockState p_217393_2_, BlockState p_217393_3_) {
    }
    public Iterable<TileEntity> getRenderedTileEntities() {
        return this.renderedTileEntities;
    }
}
