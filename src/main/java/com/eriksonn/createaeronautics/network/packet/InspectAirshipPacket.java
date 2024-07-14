package com.eriksonn.createaeronautics.network.packet;

import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.inspect.InspectUI;
import com.eriksonn.createaeronautics.network.ClientPacketHandler;
import com.simibubi.create.foundation.gui.ScreenOpener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class InspectAirshipPacket {
    public int airshipID;

    public InspectAirshipPacket(int airshipID) {
        this.airshipID = airshipID;
    }

    public InspectAirshipPacket(PacketBuffer buffer) {
        airshipID = buffer.readInt();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(airshipID);
    }


    public static void handle(InspectAirshipPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ScreenOpener.transitionTo(InspectUI.of(AirshipManager.INSTANCE.AllClientAirships.get(msg.airshipID))));


        });
        ctx.get().setPacketHandled(true);
    }

}
