package com.eriksonn.createaeronautics.events;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.ponder.CAPonderIndex;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = CreateAeronautics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataEvents {
    /**
     * Adds lang for death messages, as well as anything that must only run during `runData`
     * @author FortressNebula
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void gatherData(GatherDataEvent event) {
        // Add death messages lang
        CreateAeronautics.registrate().addLang(
                "death.attack",
                CreateAeronautics.asResource("stationary_potato_cannon"),
                "%1$s was shot by a Stationary Potato Cannon"
        );
        CreateAeronautics.registrate().addLang(
                "death.attack",
                CreateAeronautics.asResource("stationary_potato_cannon"),
                "item",
                "%1$s was shot by a Stationary Potato Cannon using %2$s"
        );
        CAPonderIndex.register();
        PonderLocalization.provideRegistrateLang(CreateAeronautics.registrate());
    }
}
