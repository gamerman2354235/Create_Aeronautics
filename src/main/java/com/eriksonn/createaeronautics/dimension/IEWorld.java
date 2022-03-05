package com.eriksonn.createaeronautics.dimension;

import net.minecraft.world.storage.ISpawnWorldInfo;

public interface IEWorld {
    ISpawnWorldInfo myGetProperties();

    void portal_setWeather(float var1, float var2, float var3, float var4);
}
