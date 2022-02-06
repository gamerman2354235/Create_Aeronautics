package com.eriksonn.createaeronautics;

import com.eriksonn.createaeronautics.index.AllParticleTypes;
import net.minecraftforge.eventbus.api.IEventBus;

public class CreateAeronauticsClient {
    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(AllParticleTypes::registerFactories);
    }
}
