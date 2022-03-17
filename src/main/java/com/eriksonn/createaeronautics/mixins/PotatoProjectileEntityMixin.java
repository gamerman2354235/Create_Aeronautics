package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.utils.PotatoProjectileEntityExtension;
import com.eriksonn.createaeronautics.blocks.stationary_potato_cannon.StationaryPotatoCannonProjectileEntity;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileEntity;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotatoProjectileEntity.class)
public class PotatoProjectileEntityMixin implements PotatoProjectileEntityExtension {
    public boolean isFromStationaryPotatoCannon = false;

    /**
     * Whenever using the PotatoProjectileEntity class with the Stationary Potato Cannon,
     * always make sure to set this to true.
     * @param value New value for the isFromStationaryPotatoCannon variable
     * @author FortressNebula
     */
    @Override
    public void setIsFromStationaryPotatoCannon(boolean value) {
        isFromStationaryPotatoCannon = value;
    }

    @Inject(
            method = "causePotatoDamage()Lnet/minecraft/util/DamageSource;",
            at = @At("RETURN"),
            remap = false,
            cancellable = true
    )
    private void onCausePotatoDamage(CallbackInfoReturnable<DamageSource> cir) {
        if (isFromStationaryPotatoCannon) {
            cir.setReturnValue(new StationaryPotatoCannonProjectileEntity.PotatoDamageSource(((PotatoProjectileEntity)(Object)this)).setProjectile());
        }
    }
}
