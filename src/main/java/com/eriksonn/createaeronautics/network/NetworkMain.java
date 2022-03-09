package com.eriksonn.createaeronautics.network;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.network.packet.AirshipAddSubcontraptionPacket;
import com.eriksonn.createaeronautics.network.packet.AirshipContraptionBlockUpdatePacket;
import com.eriksonn.createaeronautics.network.packet.AirshipDestroySubcontraptionPacket;
import com.eriksonn.createaeronautics.network.packet.AirshipUpdateSubcontraptionPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkMain {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CreateAeronautics.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static int packetID = 0;

    public static void init() {
        CHANNEL.registerMessage(packetID++, AirshipContraptionBlockUpdatePacket.class, AirshipContraptionBlockUpdatePacket::encode, AirshipContraptionBlockUpdatePacket::new, AirshipContraptionBlockUpdatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetID++, AirshipAddSubcontraptionPacket.class, AirshipAddSubcontraptionPacket::encode, AirshipAddSubcontraptionPacket::new, AirshipAddSubcontraptionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetID++, AirshipUpdateSubcontraptionPacket.class, AirshipUpdateSubcontraptionPacket::encode, AirshipUpdateSubcontraptionPacket::new, AirshipUpdateSubcontraptionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetID++, AirshipDestroySubcontraptionPacket.class, AirshipDestroySubcontraptionPacket::encode, AirshipDestroySubcontraptionPacket::new, AirshipDestroySubcontraptionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToServer(T t) {
        CHANNEL.sendToServer(t);
    }

    public static <T> void sendToPlayer(ServerPlayerEntity player, T t) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> {
            return player;
        }), t);
    }

//    public static void handle(MyMessage msg, Supplier<NetworkEvent.Context> ctx) {
//        ctx.get().enqueueWork(() -> {
//            // Work that needs to be thread-safe (most work)
//            ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
//            // Do stuff
//        });
//        ctx.get().setPacketHandled(true);
//    }
}
