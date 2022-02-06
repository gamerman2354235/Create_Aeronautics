package com.eriksonn.createaeronautics.particle;

import com.eriksonn.createaeronautics.index.AllParticleTypes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.simibubi.create.content.contraptions.particle.ICustomParticleDataWithSprite;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

public class PropellerAirParticleData implements IParticleData, ICustomParticleDataWithSprite<PropellerAirParticleData> {

    @Override
    public ParticleType<?> getType() {
        return AllParticleTypes.PROPELLER_AIR_FLOW.get();
    }

    public static final Codec<PropellerAirParticleData> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Codec.INT.fieldOf("x").forGetter(p -> p.posX),
                    Codec.INT.fieldOf("y").forGetter(p -> p.posY),
                    Codec.INT.fieldOf("z").forGetter(p -> p.posZ))
                    .apply(i, PropellerAirParticleData::new));

    public static final IParticleData.IDeserializer<PropellerAirParticleData> DESERIALIZER = new IParticleData.IDeserializer<PropellerAirParticleData>() {
        public PropellerAirParticleData fromCommand(ParticleType<PropellerAirParticleData> particleTypeIn, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            int x = reader.readInt();
            reader.expect(' ');
            int y = reader.readInt();
            reader.expect(' ');
            int z = reader.readInt();
            return new PropellerAirParticleData(x, y, z);
        }

        public PropellerAirParticleData fromNetwork(ParticleType<PropellerAirParticleData> particleTypeIn, PacketBuffer buffer) {
            return new PropellerAirParticleData(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }
    };

    final int posX;
    final int posY;
    final int posZ;
    public PropellerAirParticleData(Vector3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public PropellerAirParticleData(int posX, int posY, int posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public PropellerAirParticleData() {
        this(0, 0, 0);
    }
    @Override
    public void writeToNetwork(PacketBuffer buffer) {
        buffer.writeInt(posX);
        buffer.writeInt(posY);
        buffer.writeInt(posZ);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d %d %d", AllParticleTypes.PROPELLER_AIR_FLOW.parameter(), posX, posY, posZ);
    }

    @Override
    public IDeserializer<PropellerAirParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public Codec<PropellerAirParticleData> getCodec(ParticleType<PropellerAirParticleData> type) {
        return CODEC;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ParticleManager.IParticleMetaFactory<PropellerAirParticleData> getMetaFactory() {
        return PropellerAirParticle.Factory::new;
    }
}
