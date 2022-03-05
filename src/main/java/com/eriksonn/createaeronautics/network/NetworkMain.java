package com.eriksonn.createaeronautics.network;

import com.eriksonn.createaeronautics.CreateAeronautics;
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

public class NetworkMain {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CreateAeronautics.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        CHANNEL.registerMessage(0, StcRedirected.class, StcRedirected::encode, StcRedirected::new, StcRedirected::handle);
        //channel.registerMessage(1, StcDimensionConfirm.class, StcDimensionConfirm::encode, StcDimensionConfirm::new, StcDimensionConfirm::handle);
        //channel.registerMessage(2, StcDimensionSync.class, StcDimensionSync::encode, StcDimensionSync::new, StcDimensionSync::handle);
        //channel.registerMessage(3, CtsTeleport.class, CtsTeleport::encode, CtsTeleport::new, CtsTeleport::handle);
        //channel.registerMessage(4, StcUpdateGlobalPortals.class, StcUpdateGlobalPortals::encode, StcUpdateGlobalPortals::new, StcUpdateGlobalPortals::handle);
        //channel.registerMessage(6, StcSpawnEntity.class, StcSpawnEntity::encode, StcSpawnEntity::new, StcSpawnEntity::handle);
        //channel.registerMessage(7, CtsPlayerAction.class, CtsPlayerAction::encode, CtsPlayerAction::new, CtsPlayerAction::handle);
        //channel.registerMessage(8, CtsRightClick.class, CtsRightClick::encode, CtsRightClick::new, CtsRightClick::handle);
    }

    public static <T> void sendToServer(T t) {
        CHANNEL.sendToServer(t);
    }

    public static <T> void sendToPlayer(ServerPlayerEntity player, T t) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> {
            return player;
        }), t);
    }

    public static void sendRedirected(ServerPlayerEntity player, RegistryKey<World> dimension, IPacket t) {
        sendToPlayer(player, new StcRedirected(dimension, t));
    }

    public static IPacket getRedirectedPacket(RegistryKey<World> dimension, IPacket t) {
        return CHANNEL.toVanillaPacket(new StcRedirected(dimension, t), NetworkDirection.PLAY_TO_CLIENT);
    }


    public static IPacket createEmptyPacketByType(int messageType) {
        return ProtocolType.PLAY.createPacket(PacketDirection.CLIENTBOUND, messageType);
    }
}
