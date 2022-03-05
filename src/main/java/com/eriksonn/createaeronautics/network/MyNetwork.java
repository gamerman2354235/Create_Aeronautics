package com.eriksonn.createaeronautics.network;

import com.eriksonn.createaeronautics.CreateAeronautics;
import net.minecraft.util.ResourceLocation;

public class MyNetwork {
    public static final ResourceLocation id_stcRemote = new ResourceLocation(CreateAeronautics.MODID, "remote_stc");
    public static final ResourceLocation id_ctsRemote = new ResourceLocation(CreateAeronautics.MODID, "remote_cts");

    public MyNetwork() {
    }

    public static void init() {
        NetworkMain.init();
    }
}
