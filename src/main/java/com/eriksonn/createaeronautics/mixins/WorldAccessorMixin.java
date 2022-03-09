package com.eriksonn.createaeronautics.mixins;

import net.minecraft.world.World;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({World.class})
public interface WorldAccessorMixin {
    @Accessor(remap = false) ISpawnWorldInfo getLevelData();
}
