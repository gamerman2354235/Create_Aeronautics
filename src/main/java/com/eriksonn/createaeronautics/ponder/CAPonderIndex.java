package com.eriksonn.createaeronautics.ponder;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.index.CABlocks;
import com.eriksonn.createaeronautics.ponder.KineticScenes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;

public class CAPonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(CreateAeronautics.MODID);
    public static void register() {
        HELPER.forComponents(CABlocks.STIRLING_ENGINE).addStoryBoard("stirling_engine",KineticScenes::stirlingEngine);
    }
}
