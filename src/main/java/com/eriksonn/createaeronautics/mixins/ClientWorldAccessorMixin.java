package com.eriksonn.createaeronautics.mixins;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ClientWorld.class})
public interface ClientWorldAccessorMixin {
    @Accessor(remap = false) ClientPlayNetHandler getConnection();
    @Accessor(remap = false) void setConnection(ClientPlayNetHandler connection);
}
