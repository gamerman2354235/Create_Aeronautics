package com.eriksonn.createaeronautics;


import com.eriksonn.createaeronautics.groups.ModGroup;
import com.eriksonn.createaeronautics.index.*;
import com.simibubi.create.repack.registrate.util.NonNullLazyValue;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;
import com.simibubi.create.foundation.data.CreateRegistrate;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CreateAeronautics.MODID)
public class CreateAeronautics
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "createaeronautics";

    private static final NonNullLazyValue<CreateRegistrate> registrate = CreateRegistrate.lazy(CreateAeronautics.MODID);

    private final CADimensions registration;

    public CreateAeronautics() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        new ModGroup("main");

        CABlocks.register();
        CATileEntities.register();
        CAEntityTypes.register();
        CABlockPartials.clientInit();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        this.registration = new CADimensions();
        CADimensions var10001 = this.registration;
        modEventBus.addListener(var10001::registerDimension);
    }

    private void setup(final FMLCommonSetupEvent event)
    {}

    private void doClientStuff(final FMLClientSetupEvent event) {}

    private void enqueueIMC(final InterModEnqueueEvent event)
    {}

    private void processIMC(final InterModProcessEvent event) {}

    @SuppressWarnings("deprecation")
    public static CreateRegistrate registrate() {

        LOGGER.info("Registrate created");
        return registrate.get();
    }
}
