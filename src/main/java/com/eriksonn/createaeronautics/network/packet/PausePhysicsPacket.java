package com.eriksonn.createaeronautics.network.packet;

import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PausePhysicsPacket {
    public int airshipID;
    public boolean pause;

    public PausePhysicsPacket(int airshipID, boolean pause) {
        this.airshipID = airshipID;
        this.pause = pause;
    }

    public PausePhysicsPacket(PacketBuffer buffer) {
        airshipID = buffer.readInt();
        pause = buffer.readBoolean();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(airshipID);
        buffer.writeBoolean(pause);
    }


    public static void handle(PausePhysicsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
            if (sender != null) {
                AirshipManager.INSTANCE.AllAirships.get(msg.airshipID).playPhysics = (!msg.pause);
            }

        });
        ctx.get().setPacketHandled(true);
    }
}
