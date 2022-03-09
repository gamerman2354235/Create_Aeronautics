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

public class AirshipAddSubcontraptionPacket {
    public int plotID;
    public CompoundNBT nbt;
    public BlockPos pos;
    public UUID uuid;

    public AirshipAddSubcontraptionPacket(int plotID, CompoundNBT nbt, BlockPos pos, UUID uuid) {
        this.plotID = plotID;
        this.nbt = nbt;
        this.pos = pos;
        this.uuid = uuid;
    }

    public AirshipAddSubcontraptionPacket(PacketBuffer buffer) {
        this(buffer.readInt(), buffer.readNbt(), buffer.readBlockPos(), buffer.readUUID());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(plotID);
        buffer.writeNbt(nbt);
        buffer.writeBlockPos(pos);
        buffer.writeUUID(uuid);
    }


    public static void handle(AirshipAddSubcontraptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Make sure it's only executed on the physical client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacket(msg, ctx));
        });
        ctx.get().setPacketHandled(true);
    }
}
