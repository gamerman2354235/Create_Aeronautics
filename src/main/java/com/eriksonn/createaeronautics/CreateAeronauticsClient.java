package com.eriksonn.createaeronautics;

import com.eriksonn.createaeronautics.index.CAParticleTypes;
import com.eriksonn.createaeronautics.ponder.CAPonderIndex;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateAeronauticsClient {
    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(CAParticleTypes::registerFactories);
        modEventBus.addListener(CreateAeronauticsClient::clientInit);

    }
    public static void clientInit(final FMLClientSetupEvent event) {
        CAPonderIndex.register();
    }
}
