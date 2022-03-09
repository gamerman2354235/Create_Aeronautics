package com.eriksonn.createaeronautics.network.packet;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class AirshipContraptionBlockUpdateInfo {

    public CompoundNBT tileEntityNBT;
    public CompoundNBT blockStateNBT;
    public BlockState state;
    public int x, y, z;
    public BlockPos pos;
    public int airshipID;

    public AirshipContraptionBlockUpdateInfo(CompoundNBT nbt) {
        this.tileEntityNBT = nbt.contains("be") ? nbt.getCompound("be") : null;
        this.blockStateNBT = nbt.getCompound("state");
        this.state = NBTUtil.readBlockState(blockStateNBT);
        this.x = nbt.getInt("x");
        this.y = nbt.getInt("y");
        this.z = nbt.getInt("z");
        this.pos = new BlockPos(x, y, z);
        this.airshipID = nbt.getInt("plotId");
    }
}
