package com.eriksonn.createaeronautics.blocks.stationary_potato_cannon;

import com.eriksonn.createaeronautics.index.CABlocks;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.content.curiosities.weapons.BuiltinPotatoProjectileTypes;
import com.simibubi.create.content.curiosities.weapons.PotatoCannonProjectileType;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileEntity;
import com.simibubi.create.content.curiosities.weapons.PotatoProjectileTypeManager;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.Random;

public class StationaryPotatoCannonTileEntity extends KineticTileEntity {

    protected ItemStack currentStack;
    public LazyOptional<IItemHandlerModifiable> invHandler;
    float chargeProgress = 0;
    float fireProgress = 0;
    State state;
    boolean powered;
    private float ChargeTimer=0;
    private int BaseChargeTime=0;
    public int BarrelTimer=100;
    public int ItemTimer=100;
    int ItemRotationId;
    Random Rnd =new Random();
    @Override
    public float calculateStressApplied() {
        return 4;
    }
    public StationaryPotatoCannonTileEntity(TileEntityType<? extends StationaryPotatoCannonTileEntity> type)
    {
        super(type);
        this.currentStack = ItemStack.EMPTY;
        this.state= State.CHARGING;

    }
    public void RandomizeItemRotation()
    {
        ItemRotationId=Rnd.nextInt(10000);
    }
    public void initialize() {
        super.initialize();
        this.invHandler = LazyOptional.of(this::createHandler);

    }
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return this.isItemHandlerCap(cap) && this.invHandler != null ? this.invHandler.cast() : super.getCapability(cap, side);
    }
    public void tick() {
        super.tick();
        boolean doLogic = !this.level.isClientSide || this.isVirtual();
        if(BarrelTimer<100)
            BarrelTimer++;
        if(ItemTimer<20)
            ItemTimer++;
        List<Entity> Entities= level.getEntities(null,getInternalHitbox());
        if(this.state == State.CHARGING)
        {
            if(!currentStack.isEmpty())
            {
                int N=GetProjectilyType().getReloadTicks();
                if(BaseChargeTime!=N) {
                    BaseChargeTime = N;
                    sendData();
                }

            }else
            if(!Entities.isEmpty())
            {
                int N=20;
                if(BaseChargeTime!=N) {
                    BaseChargeTime = N;
                    sendData();
                }
            }
            if(BaseChargeTime>0) {
                chargeProgress += getChargeUpSpeed();
                if (chargeProgress > 1) {
                    chargeProgress = 1;

                    this.state = State.CHARGED;
                    sendData();
                }
            }
        }else
        if(this.state == State.CHARGED)
        {
            chargeProgress=1;
            if(this.powered)
            {
                this.state = State.FIRING;
                BarrelTimer=0;
                sendData();
            }
        }else
        if(this.state == State.FIRING)
        {
            if(BarrelTimer>1) {
                this.state = State.CHARGING;
                chargeProgress = 0;
                BaseChargeTime=0;
                float pitch=1;
                boolean DoFwomp = !currentStack.isEmpty();

                if(!Entities.isEmpty())
                    DoFwomp=true;
                if(DoFwomp)
                {
                    if(!currentStack.isEmpty()) {
                        PotatoCannonProjectileType projectileType = GetProjectilyType();
                        pitch = projectileType.getSoundPitch();
                    }
                    AllSoundEvents.FWOOMP.playOnServer(level, worldPosition, 1,pitch );
                }else
                {
                    level.playSound(null, worldPosition, SoundEvents.DISPENSER_FAIL, SoundCategory.BLOCKS, 1, 1.2f);
                }
                Vector3d barrelPos = new Vector3d(getBlockPos().getX()+0.5,getBlockPos().getY()+0.5,getBlockPos().getZ()+0.5).add(getAimingVector());

                //IParticleData dat = new ;
                //Registry.PARTICLE_TYPE.get(new ResourceLocation("?"));
                for(int i =0;i<8;i++)
                {
                    Vector3d Vel =getAimingVector();
                    Vel=Vel.add(new Vector3d(Rnd.nextDouble()-0.5,Rnd.nextDouble()-0.5,Rnd.nextDouble()-0.5).scale(1.0));
                    Vel=Vel.scale(1.5);
                    this.level.addParticle(new AirParticleData(0.5f,0.1f), barrelPos.x, barrelPos.y, barrelPos.z, Vel.x, Vel.y, Vel.z);
                }
                ItemTimer=0;
                Shoot();
                RandomizeItemRotation();
                sendData();

                for (Entity e:Entities) {
                    e.setDeltaMovement(getAimingVector());
                    e.setPos(barrelPos.x,barrelPos.y,barrelPos.z);
                }
            }
        }
    }
    PotatoCannonProjectileType GetProjectilyType()
    {
        return PotatoProjectileTypeManager.getTypeForStack(currentStack).orElse(BuiltinPotatoProjectileTypes.FALLBACK);
    }
    void Shoot()
    {
        if(!currentStack.isEmpty())
        {
            Vector3d barrelPos = new Vector3d(getBlockPos().getX()+0.5,getBlockPos().getY()+0.5,getBlockPos().getZ()+0.5).add(getAimingVector());
            PotatoCannonProjectileType projectileType = GetProjectilyType();
            Vector3d motion = getAimingVector().scale((double)projectileType.getVelocityMultiplier());
            float soundPitch = projectileType.getSoundPitch() + (Create.RANDOM.nextFloat() - 0.5F) / 4.0F;
            boolean spray = projectileType.getSplit() > 1;
            Vector3d sprayBase = VecHelper.rotate(new Vector3d(0.0D, 0.1D, 0.0D), (double)(360.0F * Create.RANDOM.nextFloat()), Direction.Axis.Z);
            float sprayChange = 360.0F / (float)projectileType.getSplit();

            for(int i = 0; i < projectileType.getSplit(); ++i) {
                PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(this.level);
                // == This is the new code that fixes the death message. ==
                ((PotatoProjectileEntityFix) projectile).setIsFromStationaryPotatoCannon(true);
                // ========================================================
                projectile.setItem(currentStack);

                //projectile.setEnchantmentEffectsFromCannon(stack);
                Vector3d splitMotion = motion;
                if (spray) {
                    float imperfection = 40.0F * (Create.RANDOM.nextFloat() - 0.5F);
                    Vector3d sprayOffset = VecHelper.rotate(sprayBase, (double)((float)i * sprayChange + imperfection), Direction.Axis.Z);
                    splitMotion = motion.add(VecHelper.lookAt(sprayOffset, motion));
                }

                if (i != 0) {
                    //projectile.recoveryChance = 0.0F;
                }

                projectile.setPos(barrelPos.x, barrelPos.y, barrelPos.z);
                projectile.setDeltaMovement(splitMotion);
                //projectile.setOwner(this);
                level.addFreshEntity(projectile);




            }
            currentStack.shrink(1);

        }

    }
    AxisAlignedBB getInternalHitbox()
    {
        Vector3d hitboxPos = getAimingVector().scale(0.28);
        return new AxisAlignedBB(getBlockPos()).deflate(5/8.0).move(hitboxPos);
    }
    protected Vector3d getAimingVector() {
        return !CABlocks.STATIONARY_POTATO_CANNON.has(this.getBlockState()) ? Vector3d.ZERO : Vector3d.atLowerCornerOf(((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getNormal());
    }
    public void updateSignal() {
        boolean shouldPower = this.level.hasNeighborSignal(this.worldPosition);
        if (shouldPower != this.powered) {
            this.powered = shouldPower;
            this.sendData();
        }
    }
    private IItemHandlerModifiable createHandler() {
        return new CannonItemHandler(this);
    }
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }
    public boolean shouldRenderNormally() {
        return true;
    }
    public void write(CompoundNBT compound, boolean clientPacket) {
        super.write(compound, clientPacket);

        compound.put("CurrentStack", this.currentStack.serializeNBT());
        compound.putInt("BarrelTimer", BarrelTimer);
        compound.putFloat("ChargeTimer", ChargeTimer);
        compound.putInt("BaseChargeTime",BaseChargeTime);
        compound.putInt("ItemTimer", ItemTimer);
        compound.putInt("State",state.ordinal());
        compound.putInt("ItemRotationId",ItemRotationId);
    }
    protected void fromTag(BlockState blockState, CompoundNBT compound, boolean clientPacket) {
        super.fromTag(blockState, compound, clientPacket);
        this.currentStack = ItemStack.of(compound.getCompound("CurrentStack"));
        BarrelTimer=compound.getInt("BarrelTimer");
        ChargeTimer=compound.getFloat("ChargeTimer");
        BaseChargeTime=compound.getInt("BaseChargeTime");
        ItemTimer=compound.getInt("ItemTimer");
        state = State.values()[compound.getInt("State")];
        ItemRotationId=compound.getInt("ItemRotationId");
    }
    public float getChargeUpSpeed(){
        if(BaseChargeTime==0)
            return 0;
        return (Math.abs(this.getSpeed())/(64*BaseChargeTime));//same charge rate as normal potato cannon at 64 rpm
    }
    public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip,isPlayerSneaking);
        tooltip.add(componentSpacing.plainCopy()
                .append(""));
        tooltip.add(componentSpacing.plainCopy()
                .append("Stationary potato cannon:"));
        if(currentStack.isEmpty())
        {
            tooltip.add(componentSpacing.plainCopy().plainCopy()
                    .append("Ammo: ")
                    .withStyle(TextFormatting.GRAY).append("None").withStyle(TextFormatting.RED));
        }else
        {
            String _attack = "potato_cannon.ammo.attack_damage";
            String _reload = "potato_cannon.ammo.reload_ticks";
            String _knockback = "potato_cannon.ammo.knockback";
            PotatoCannonProjectileType projectileType = GetProjectilyType();
            tooltip.add(componentSpacing.plainCopy().plainCopy()
                    .append("Ammo: "+new TranslationTextComponent(currentStack.getItem()
                            .getDescriptionId(currentStack)).getString()+" x"+ currentStack.getCount())
                    .withStyle(TextFormatting.GRAY));
            StringTextComponent spacing = new StringTextComponent(componentSpacing.plainCopy().getString()+" ");
            TextFormatting green = TextFormatting.GREEN;
            TextFormatting darkGreen = TextFormatting.DARK_GREEN;

            float additionalDamageMult=1;
            float additionalKnockback=0;
            float damageF = projectileType.getDamage() * additionalDamageMult;
            IFormattableTextComponent damage = new StringTextComponent(
                    damageF == MathHelper.floor(damageF) ? "" + MathHelper.floor(damageF) : "" + damageF);
            IFormattableTextComponent reloadTicks = new StringTextComponent("" + projectileType.getReloadTicks());
            IFormattableTextComponent knockback =
                    new StringTextComponent("" + (projectileType.getKnockback() + additionalKnockback));

            damage = damage.withStyle(additionalDamageMult > 1 ? green : darkGreen);
            knockback = knockback.withStyle(additionalKnockback > 0 ? green : darkGreen);
            reloadTicks = reloadTicks.withStyle(darkGreen);
            tooltip.add(spacing.plainCopy()
                    .append(Lang.translate(_attack, damage)
                            .withStyle(darkGreen)));
            tooltip.add(spacing.plainCopy()
                    .append(Lang.translate(_reload, reloadTicks)
                            .withStyle(darkGreen)));
            tooltip.add(spacing.plainCopy()
                    .append(Lang.translate(_knockback, knockback)
                            .withStyle(darkGreen)));
        }
        return true;
    }

    @Override
    public World getWorld() {
        return this.level;
    }
    public static enum State {
        CHARGED,
        FIRING,
        CHARGING;

        private State() {
        }
    }
}
