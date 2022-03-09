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

public class AirshipUpdateSubcontraptionPacket {
    public int plotID;
    public CompoundNBT nbt;
    public UUID uuid;

    public AirshipUpdateSubcontraptionPacket(int plotID, CompoundNBT nbt, UUID uuid) {
        this.plotID = plotID;
        this.nbt = nbt;
        this.uuid = uuid;
    }

    public AirshipUpdateSubcontraptionPacket(PacketBuffer buffer) {
        this(buffer.readInt(), buffer.readNbt(), buffer.readUUID());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(plotID);
        buffer.writeNbt(nbt);
        buffer.writeUUID(uuid);
    }


    public static void handle(AirshipUpdateSubcontraptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Make sure it's only executed on the physical client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacket(msg, ctx));
        });
        ctx.get().setPacketHandled(true);
    }
}
