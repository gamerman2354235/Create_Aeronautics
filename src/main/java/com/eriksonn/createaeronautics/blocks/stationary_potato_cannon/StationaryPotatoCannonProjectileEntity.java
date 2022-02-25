package com.eriksonn.createaeronautics.blocks.stationary_potato_cannon;

import com.simibubi.create.content.curiosities.weapons.PotatoProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;

/**
 * Just exists to have that custom PotatoDamageSource class
 * @author FortressNebula
 */
public class StationaryPotatoCannonProjectileEntity extends PotatoProjectileEntity {
    public StationaryPotatoCannonProjectileEntity(EntityType<? extends DamagingProjectileEntity> type, World world) {
        super(type, world);
    }

    /**
     * Custom PotatoDamageSource class that extends EntityDamageSource rather than IndirectEntityDamageSource.
     * This means that we avoid any weirdness with the death message.
     * @author FortressNebula
     */
    public static class PotatoDamageSource extends EntityDamageSource {

        public PotatoDamageSource(Entity source) {
            super("createaeronautics.stationary_potato_cannon", source);
        }
    }
}
