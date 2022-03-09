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

public class AirshipDestroySubcontraptionPacket {
    public int plotID;
    public UUID uuid;

    public AirshipDestroySubcontraptionPacket(int plotID, UUID uuid) {
        this.plotID = plotID;
        this.uuid = uuid;
    }

    public AirshipDestroySubcontraptionPacket(PacketBuffer buffer) {
        this(buffer.readInt(), buffer.readUUID());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(plotID);
        buffer.writeUUID(uuid);
    }


    public static void handle(AirshipDestroySubcontraptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Make sure it's only executed on the physical client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacket(msg, ctx));
        });
        ctx.get().setPacketHandled(true);
    }
}
