package com.eriksonn.createaeronautics.network;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.index.CAEntityTypes;
import com.eriksonn.createaeronautics.network.packet.*;
import com.simibubi.create.AllEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ClientPacketHandler {
    public HashMap<Integer, AirshipContraptionEntity> cache = new HashMap<>();

    public static void handlePacket(AirshipContraptionBlockUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        Map<Integer, AirshipContraptionEntity> allAirships = AirshipManager.INSTANCE.AllClientAirships;

        AirshipContraptionBlockUpdateInfo info = msg.getInfo();

        if (allAirships.containsKey(info.airshipID)) {
            AirshipContraptionEntity airship = allAirships.get(info.airshipID);

            airship.handle(info);
        }
    }

    public static void handlePacket(AirshipAddSubcontraptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        Map<Integer, AirshipContraptionEntity> allAirships = AirshipManager.INSTANCE.AllClientAirships;

        if (allAirships.containsKey(msg.plotID)) {
            AirshipContraptionEntity airship = allAirships.get(msg.plotID);

            airship.addSubcontraptionClient(msg.nbt, msg.uuid, msg.pos);
        }
    }

    public static void handlePacket(AirshipUpdateSubcontraptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        Map<Integer, AirshipContraptionEntity> allAirships = AirshipManager.INSTANCE.AllClientAirships;

        if (allAirships.containsKey(msg.plotID)) {
            AirshipContraptionEntity airship = allAirships.get(msg.plotID);

            airship.updateSubcontraptionClient(msg.uuid, msg.nbt);
        }
    }

    public static void handlePacket(AirshipDestroySubcontraptionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        Map<Integer, AirshipContraptionEntity> allAirships = AirshipManager.INSTANCE.AllClientAirships;

        if (allAirships.containsKey(msg.plotID)) {
            AirshipContraptionEntity airship = allAirships.get(msg.plotID);

            airship.destroySubcontraptionClient(msg.uuid);
        }
    }

    public static void handlePacket(AirshipBEUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        Map<Integer, AirshipContraptionEntity> allAirships = AirshipManager.INSTANCE.AllClientAirships;

        if (allAirships.containsKey(msg.airshipID)) {
            AirshipContraptionEntity airship = allAirships.get(msg.airshipID);

            TileEntity tileEntity = airship.fakeClientWorld.getBlockEntity(msg.pos);
            if (tileEntity == null) return;

            tileEntity.handleUpdateTag(airship.fakeClientWorld.getBlockState(msg.pos), msg.nbt);
            tileEntity.onDataPacket(ctx.get().getNetworkManager(), new SUpdateTileEntityPacket(msg.pos, msg.type, msg.nbt));
            tileEntity.setLevelAndPosition(airship.fakeClientWorld, msg.pos);
        }
    }
}
