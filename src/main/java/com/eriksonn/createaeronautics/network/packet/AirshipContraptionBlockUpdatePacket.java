package com.eriksonn.createaeronautics.network.packet;

import com.eriksonn.createaeronautics.network.ClientPacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AirshipContraptionBlockUpdatePacket {

    CompoundNBT nbt;

    public AirshipContraptionBlockUpdatePacket(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public AirshipContraptionBlockUpdateInfo getInfo() {
        return new AirshipContraptionBlockUpdateInfo(nbt);
    }

    public AirshipContraptionBlockUpdatePacket(PacketBuffer buffer) {
        this(buffer.readNbt());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeNbt(nbt);
    }


    public static void handle(AirshipContraptionBlockUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Make sure it's only executed on the physical client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacket(msg, ctx));
        });
        ctx.get().setPacketHandled(true);
    }
}
