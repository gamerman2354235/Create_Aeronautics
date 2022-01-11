package com.eriksonn.createaeronautics.index;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.jozufozu.flywheel.core.PartialModel;
import net.minecraft.util.ResourceLocation;

public class CABlockPartials {

    public static final PartialModel
    CANNON_BARREL = get("stationary_potato_cannon/barrel"),
    CANNON_BELLOW = get("stationary_potato_cannon/bellow");

    private static PartialModel get(String path) {
        ResourceLocation L = new ResourceLocation(CreateAeronautics.MODID, "block/" + path);
        return new PartialModel(L);
    }
    public static void clientInit() {
        // init static fields
    }
}
