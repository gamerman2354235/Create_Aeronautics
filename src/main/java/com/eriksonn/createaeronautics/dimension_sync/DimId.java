package com.eriksonn.createaeronautics.dimension_sync;


import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class DimId {
    private static final boolean useIntegerId = true;

    public DimId() {
    }

    public static void writeWorldId(PacketBuffer buf, RegistryKey<World> dimension, boolean isClient) {
        DimensionIdRecord record = isClient ? DimensionIdRecord.clientRecord : DimensionIdRecord.serverRecord;
        int intId = record.getIntId(dimension);
        buf.writeInt(intId);
    }

    public static RegistryKey<World> readWorldId(PacketBuffer buf, boolean isClient) {
        if (isClient && (FMLEnvironment.dist == Dist.DEDICATED_SERVER)) {
            throw new IllegalStateException("oops");
        } else {
            DimensionIdRecord record = isClient ? DimensionIdRecord.clientRecord : DimensionIdRecord.serverRecord;
            int intId = buf.readInt();
            return record.getDim(intId);
        }
    }

    public static RegistryKey<World> idToKey(ResourceLocation identifier) {
        return RegistryKey.create(Registry.DIMENSION_REGISTRY, identifier);
    }

    public static RegistryKey<World> idToKey(String str) {
        return idToKey(new ResourceLocation(str));
    }

    public static void putWorldId(CompoundNBT tag, String tagName, RegistryKey<World> dim) {
        tag.putString(tagName, dim.location().toString());
    }

    public static RegistryKey<World> getWorldId(CompoundNBT tag, String tagName, boolean isClient) {
        INBT term = tag.get(tagName);
        if (term instanceof IntNBT) {
            int intId = ((IntNBT)term).getAsInt();
            DimensionIdRecord record = isClient ? DimensionIdRecord.clientRecord : DimensionIdRecord.serverRecord;
            return record.getDim(intId);
        } else if (term instanceof StringNBT) {
            String id = ((StringNBT)term).getAsString();
            return idToKey(id);
        } else {
            throw new RuntimeException("Invalid Dimension Record " + term);
        }
    }
}
