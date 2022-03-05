package com.eriksonn.createaeronautics.network;


import com.eriksonn.createaeronautics.dimension_sync.DimId;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.function.Supplier;

public class StcRedirected {
    public RegistryKey<World> dimension;
    public int packetId;
    public IPacket packet;
    private static int reportedErrors = 0;

    public StcRedirected(RegistryKey<World> dimensionType, IPacket packet) {
        this.dimension = dimensionType;
        Validate.notNull(dimensionType);
        this.packet = packet;

        try {
            this.packetId = ProtocolType.PLAY.getPacketId(PacketDirection.CLIENTBOUND, packet);
        } catch (Exception var4) {
            throw new IllegalStateException(var4);
        }
    }

    public StcRedirected(PacketBuffer buf) {
        this.dimension = DimId.readWorldId(buf, true);
        this.packetId = buf.readInt();
        this.packet = NetworkMain.createEmptyPacketByType(this.packetId);

        try {
            this.packet.read(buf);
        } catch (IOException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    public static void doProcessRedirectedPacket(RegistryKey<World> dimension, IPacket packet) {
        CommonNetworkClient.processRedirectedPacket(dimension, packet);
    }

    public void encode(PacketBuffer buf) {
        DimId.writeWorldId(buf, this.dimension, false);
        buf.writeInt(this.packetId);

        try {
            this.packet.write(buf);
        } catch (IOException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (this.dimension == null) {
            throw new IllegalStateException("Redirected Packet without Dimension info " + this.packet);
        } else {
            ((NetworkEvent.Context)context.get()).enqueueWork(() -> {
                doProcessRedirectedPacket(this.dimension, this.packet);
            });
            ((NetworkEvent.Context)context.get()).setPacketHandled(true);
        }
    }
}
