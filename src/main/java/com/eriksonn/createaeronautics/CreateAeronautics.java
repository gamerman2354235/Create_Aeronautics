package com.eriksonn.createaeronautics;


import com.eriksonn.createaeronautics.groups.CAItemGroups;
import com.eriksonn.createaeronautics.index.*;
import com.simibubi.create.repack.registrate.util.NonNullLazyValue;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.foundation.data.CreateRegistrate;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CreateAeronautics.MODID)
public class CreateAeronautics
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "createaeronautics";

    private static final NonNullLazyValue<CreateRegistrate> registrate = CreateRegistrate.lazy(CreateAeronautics.MODID);

    public CreateAeronautics() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        CABlocks.register();
        CATileEntities.register();
        CAEntityTypes.register();
        CABlockPartials.clientInit();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CADimensions registration = new CADimensions();
        modEventBus.addListener(registration::registerDimension);

        modEventBus.addGenericListener(ParticleType.class, CAParticleTypes::register);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateAeronauticsClient.onCtorClient(modEventBus, MinecraftForge.EVENT_BUS));
    }
    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }

    @SuppressWarnings("deprecation")
    public static CreateRegistrate registrate() {

        LOGGER.info("Registrate created");
        return registrate.get();
    }
}
