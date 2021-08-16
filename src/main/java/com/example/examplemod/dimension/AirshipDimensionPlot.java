package com.example.examplemod.dimension;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class AirshipDimensionPlot {
    private final int id;
    private final BlockPos size;

    private static final String TAG_ID = "id";

    private static final String TAG_SIZE = "size";

    private static final String TAG_LAST_TRANSITION = "last_transition";

    private static final int REGION_SIZE = 512;

    public static final int MAX_SIZE = 128;
    public AirshipDimensionPlot(int id, BlockPos size) {
        this.id = id;
        this.size = size;
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
            throw new IllegalArgumentException("Plot size " + size + " is smaller than minimum size.");
        }
        if (size.getX() > MAX_SIZE || size.getY() > MAX_SIZE || size.getZ() > MAX_SIZE) {
            throw new IllegalArgumentException("Plot size " + size + " exceeds maximum size of " + MAX_SIZE);
        }
    }
    public int getId() {
        return id;
    }
    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(TAG_ID, id);
        tag.put(TAG_SIZE, NBTUtil.writeBlockPos(size));
        //if (lastTransition != null) {
        //    tag.put(TAG_LAST_TRANSITION, lastTransition.toTag());
        //}
        return tag;
    }

    public static AirshipDimensionPlot fromTag(CompoundNBT tag) {
        int id = tag.getInt(TAG_ID);
        BlockPos size = NBTUtil.readBlockPos(tag.getCompound(TAG_SIZE));
        AirshipDimensionPlot plot = new AirshipDimensionPlot(id, size);

        if (tag.contains(TAG_LAST_TRANSITION, Constants.NBT.TAG_COMPOUND)) {
            //plot.lastTransition = TransitionInfo.fromTag(tag.getCompound(TAG_LAST_TRANSITION));
        }
        return plot;
    }
}
