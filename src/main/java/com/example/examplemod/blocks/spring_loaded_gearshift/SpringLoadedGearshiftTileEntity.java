package com.example.examplemod.blocks.spring_loaded_gearshift;

import com.simibubi.create.content.contraptions.KineticNetwork;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

public class SpringLoadedGearshiftTileEntity extends SplitShaftTileEntity {

    float currentAngle =0;
    float currentModifier=1;
    int stage =0;//0=input driven,1=max-clamped,2=spring powered
    int flicker=0;
    float drivenSpeed=0;
    float stressimpact=8;
    float springbackSpeed=0;

    boolean updateNextTick=false;
    protected ScrollValueBehaviour maxAngle;
    public Direction outputFace;

    public SpringLoadedGearshiftTileEntity(TileEntityType<?> typeIn) {
        super(typeIn);
    }
    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        Integer max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get();

        maxAngle = new ScrollValueBehaviour(new StringTextComponent("Angle"), this, new SpringGearshiftValueBoxTransform());
        maxAngle.between(1, 180);
        maxAngle.value = 90;
        maxAngle.moveText(new Vector3d(0, 0, 0));
        maxAngle.withStepFunction(SpringLoadedGearshiftTileEntity::step);
        behaviours.add(maxAngle);
    }
    private class SpringGearshiftValueBoxTransform extends ValueBoxTransform.Sided {


        @Override
        protected Vector3d getSouthLocation() {
            return VecHelper.voxelSpace(8, 8f, 14);
        }

        @Override
        protected float getScale() {
            return 0.3f;
        }
    }
    public static int step(ScrollValueBehaviour.StepContext context) {
        int current = context.currentValue;
        int step = 5;

        if (context.shift) {
            step=1;
        }
        int out = (int) (current + (context.forward ? step : -step) == 0 ? step + 1 : step);
        if(!context.shift)
        {

            if(context.forward) {
                out+=current;
                out = (int) Math.floor(out / 5) * 5;
                out-=current;
            }
            else
            {
                out-=current;
                out = (int)Math.floor((out)/5.0)*5;
                out+=current;
            }


        }
        return out;
    }
    @Override
    public float calculateAddedStressCapacity() {
        return lastCapacityProvided = (stage ==2 ? stressimpact : 0);
    }
    @Override
    public float getGeneratedSpeed() {
        return stage ==2 ? -Math.abs(drivenSpeed)*Math.signum(currentAngle) : 0;
    }
    @Override
    public float calculateStressApplied() {
        return stage ==0 ? stressimpact:0;
    }
    public void tick() {

        flicker = 0;
        super.tick();
        if(updateNextTick)
        {
            updateNextTick=false;
            stage=2;
            currentModifier=0;
            detachKinetics();
            updateGeneratedRotation();

            //attachKinetics();
            sendData();
        }
        double degreesPerTick = KineticTileEntity.convertToAngular(speed);
        if (stage == 1) {
            degreesPerTick = 0;
            if (speed * currentAngle < 0)
                stage = 0;
        }
        if (stage == 2) {
            degreesPerTick=KineticTileEntity.convertToAngular(-Math.abs(drivenSpeed)*Math.signum(currentAngle));

            if(currentAngle*speed>=0)
            {
                //setSpeed(0);
                stage=0;
                //updateGeneratedRotation();

                //stage=2;
                //currentModifier=0;


                updateGeneratedRotation();
                detachKinetics();
                attachKinetics();
                sendData();
                currentModifier=1;
                currentAngle=0;
                degreesPerTick=0;

            }
        }
        else
        {
            drivenSpeed = speed;
        }



        currentAngle+=degreesPerTick;
        if(Math.abs(currentAngle)>=maxAngle.value&&speed*currentAngle>0&& stage ==0)
        {
            stage =1;
            currentModifier=0.000000000001f;
            detachKinetics();
            attachKinetics();
            sendData();
        }

    }
    @Override
    public void sendData()
    {
        super.sendData();
        flicker++;
        if(flicker>10)
        {
            onSpeedChanged(0);
        }
    }
    @Override
    public void onSpeedChanged(float previousspeed)
    {
        super.onSpeedChanged(previousspeed);
        if(stage!=2&&speed==0)
        {
            stage=2;
            ////updateGeneratedRotation();
            currentModifier=0;
            speed=drivenSpeed;
            //System.out.println("detatch start");
            detachKinetics();
            //System.out.println("detatch end");
            speed=0;

            //attachKinetics();


            //updateGeneratedRotation();
//
            //sendData();


            //removeSource();

            //updateGeneratedRotation();

            float speed = this.getGeneratedSpeed();
            float prevSpeed = this.speed;


            //this.onSpeedChanged(prevSpeed);
            if (!this.level.isClientSide) {
                if (prevSpeed != speed) {
                    if (!this.hasSource()) {
                        IRotate.SpeedLevel levelBefore = IRotate.SpeedLevel.of(this.speed);
                        IRotate.SpeedLevel levelafter = IRotate.SpeedLevel.of(speed);
                        if (levelBefore != levelafter) {
                            this.effects.queueRotationIndicators();
                        }
                    }
                    //System.out.println("apply speed start");
                    this.applyNewSpeed(prevSpeed, speed);
                    //System.out.println("apply speed end");
                }
                this.sendData();

            }


            detachKinetics(getWorld(),worldPosition,true);

            //updateNextTick=true;
            //attachKinetics();
            springbackSpeed = -Math.abs(drivenSpeed)*Math.signum(currentAngle);
        }
    }
    @Override
    public float getRotationSpeedModifier(Direction face) {
        float out =1;
        if (isVirtual())
            out = 1;
        else if(stage==2&&face!=outputFace)
            out = 0;
        else if(!hasSource() || face == getSourceFacing())
        {
            out = 1;

        }else
        {
            if(stage!=2)
            {
                outputFace=face;
            }
            out = currentModifier;
        }
        //System.out.println("requested speed from "+face.toString()+" is "+ out);
        return out;
    }
    @Override
    public void write(CompoundNBT compound, boolean clientPacket) {

        compound.putInt("stage", stage);
        compound.putFloat("currentAngle", currentAngle);
        compound.putFloat("currentModifier", currentModifier);
        compound.putFloat("drivenSpeed", drivenSpeed);

        super.write(compound, clientPacket);
    }

    @Override
    protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
        stage = compound.getInt("stage");
        currentAngle = compound.getFloat("currentAngle");
        currentModifier = compound.getFloat("currentModifier");
        drivenSpeed = compound.getFloat("drivenSpeed");
        super.fromTag(state, compound, clientPacket);
    }
    @Override
    public World getWorld() {
        return getLevel();
    }
    public boolean reActivateSource;
    public void removeSource() {
        if (this.hasSource() && this.isSource()) {
            this.reActivateSource = true;
        }

        super.removeSource();
    }

    public void setSource(BlockPos source) {
        super.setSource(source);
        TileEntity tileEntity = this.level.getBlockEntity(source);
        if (tileEntity instanceof KineticTileEntity) {
            KineticTileEntity sourceTe = (KineticTileEntity)tileEntity;
            if (this.reActivateSource && Math.abs(sourceTe.getSpeed()) >= Math.abs(this.getGeneratedSpeed())) {
                this.reActivateSource = false;
            }

        }
    }

    public void updateGeneratedRotation() {
        float speed = this.getGeneratedSpeed();
        float prevSpeed = this.speed;
        if (!this.level.isClientSide) {
            if (prevSpeed != speed) {
                if (!this.hasSource()) {
                    IRotate.SpeedLevel levelBefore = IRotate.SpeedLevel.of(this.speed);
                    IRotate.SpeedLevel levelafter = IRotate.SpeedLevel.of(speed);
                    if (levelBefore != levelafter) {
                        this.effects.queueRotationIndicators();
                    }
                }

                this.applyNewSpeed(prevSpeed, speed);
            }

            if (this.hasNetwork() && speed != 0.0F) {
                KineticNetwork network = this.getOrCreateNetwork();
                this.notifyStressCapacityChange(this.calculateAddedStressCapacity());
                this.getOrCreateNetwork().updateStressFor(this, this.calculateStressApplied());
                network.updateStress();
            }

            //this.onSpeedChanged(prevSpeed);
            this.sendData();
        }
    }
    public void applyNewSpeed(float prevSpeed, float speed) {
        if (speed == 0.0F) {
            if (this.hasSource()) {
                this.notifyStressCapacityChange(0.0F);
                this.getOrCreateNetwork().updateStressFor(this, this.calculateStressApplied());
            } else {
                this.detachKinetics();
                this.setSpeed(0.0F);
                this.setNetwork((Long)null);
            }
        } else if (prevSpeed == 0.0F) {
            this.setSpeed(speed);
            this.setNetwork(this.createNetworkId());

            //BlockPos InputPos = getBlockPos().offset(outputFace.getOpposite().getNormal());
            //KineticTileEntity E = (KineticTileEntity)getWorld().getBlockEntity(InputPos);
            //if(E != null)
            //{
            //    E.network=this.network;
            //    E.setSpeed(speed);
            //}

            this.attachKinetics();

            //if(E != null)
            //{
            //    E.network=null;
            //    E.setSpeed(0);
            //}

        } else if (this.hasSource()) {
            if (Math.abs(prevSpeed) >= Math.abs(speed)) {
                if (Math.signum(prevSpeed) != Math.signum(speed)) {
                    this.level.destroyBlock(this.worldPosition, true);
                }

            } else {
                this.detachKinetics();
                this.setSpeed(speed);
                this.source = null;
                this.setNetwork(this.createNetworkId());
                this.attachKinetics();
            }
        } else {
            this.detachKinetics();
            this.setSpeed(speed);
            this.attachKinetics();
        }
    }
    public Long createNetworkId() {
        return this.worldPosition.asLong();
    }
    protected void notifyStressCapacityChange(float capacity) {
        this.getOrCreateNetwork().updateCapacityFor(this, capacity);
    }

    public void detachKinetics(World worldIn, BlockPos pos, boolean reAttachNextTick) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof KineticTileEntity) {
            RotationPropagator.handleRemoved(worldIn, pos, (KineticTileEntity)te);
            if (reAttachNextTick) {

                worldIn.getBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 0, TickPriority.EXTREMELY_HIGH);
            }
        }
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof KineticTileEntity) {
            KineticTileEntity kte = (KineticTileEntity)te;
            RotationPropagator.handleAdded(worldIn, pos, kte);
        }
    }
}
