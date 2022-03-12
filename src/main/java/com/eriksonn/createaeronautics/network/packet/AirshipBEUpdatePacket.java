package com.eriksonn.createaeronautics.network.packet;

import com.eriksonn.createaeronautics.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class AirshipBEUpdatePacket {
    public CompoundNBT nbt;
    public BlockPos pos;
    public int airshipID, type;

    public AirshipBEUpdatePacket(int type, CompoundNBT nbt, BlockPos pos, int airshipID) {
        this.type = type;
        this.nbt = nbt;
        this.pos = pos;
        this.airshipID = airshipID;
    }

    public AirshipBEUpdatePacket(PacketBuffer buffer) {
        this(buffer.readInt(), buffer.readNbt(), buffer.readBlockPos(), buffer.readInt());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(type);
        buffer.writeNbt(nbt);
        buffer.writeBlockPos(pos);
        buffer.writeInt(airshipID);
    }


    public static void handle(AirshipBEUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Make sure it's only executed on the physical client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacket(msg, ctx));
        });
        ctx.get().setPacketHandled(true);
    }
}
