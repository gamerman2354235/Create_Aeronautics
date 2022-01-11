package com.eriksonn.createaeronautics.blocks.torsion_spring;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.KineticNetwork;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.DirectionalExtenderScrollOptionSlot;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftTileEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkFrequencySlot;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class TorsionSpringTileEntity extends SplitShaftTileEntity {

    float currentAngle =0;
    float currentModifier=1;
    int stage =0;//0=input driven,1=max-clamped,2=spring powered
    int flicker=0;
    float drivenSpeed=0;
    float stressimpact=8;
    float springbackSpeed=0;
    boolean externalChange=false;
    boolean powered;

    boolean updateNextTick=false;
    protected ScrollValueBehaviour maxAngle;
    protected ScrollOptionBehaviour<MovementMode> movementMode;
    public Direction outputFace;

    public TorsionSpringTileEntity(TileEntityType<?> typeIn) {
        super(typeIn);
    }
    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        //Integer max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get();

        maxAngle = new ScrollValueBehaviour(new StringTextComponent("Angle"), this, new TorsionSpringValueBoxTransform());
        maxAngle.between(1, 360);

        maxAngle.value = 90;
        maxAngle.withStepFunction(TorsionSpringTileEntity::step);
        behaviours.add(maxAngle);
        movementMode=new ScrollOptionBehaviour<>(
                MovementMode.class,Lang.translate("contraptions.movement_mode"),this,new TorsionSpringValueBoxTransform2()
        );
        //behaviours.add(movementMode);
        Pair<ValueBoxTransform, ValueBoxTransform> slots =
                ValueBoxTransform.Dual.makeSlots(RedstoneLinkFrequencySlot::new);
        SpringBehaviour b = new SpringBehaviour(this,slots);
        //behaviours.add(b);
    }
    private class TorsionSpringValueBoxTransform extends ValueBoxTransform.Sided {


        @Override
        protected Vector3d getSouthLocation() {
            return VecHelper.voxelSpace(8, 8f, 14);
        }

        @Override
        protected float getScale() {
            return 0.3f;
        }

        protected ValueBoxTransform getMovementModeSlot() {
            return new DirectionalExtenderScrollOptionSlot((state, d) -> {
                Direction.Axis axis = d.getAxis();


                Direction.Axis shaftAxis = ((IRotate) state.getBlock()).getRotationAxis(state);

                return shaftAxis != axis;
            });
        }
        @Override
        protected boolean isSideActive(BlockState state, Direction direction)
        {
            return !state.getValue(TorsionSpringBlock.AXIS).test(direction);
        }
        @Override
        protected void rotate(BlockState state, MatrixStack ms) {
            float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
            float xRot = getSide() == Direction.UP ? 90 : getSide() == Direction.DOWN ? 270 : 0;
            MatrixTransformStack.of(ms)
                    .rotateY(yRot)
                    .rotateX(xRot);
        }
        @Override
        protected Vector3d getLocalOffset(BlockState state) {
            Vector3d location = getSouthLocation();
            if(state.getValue(TorsionSpringBlock.AXIS).isVertical()) {
                location=location.add(VecHelper.voxelSpace(-3, 0, 0));
            }
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.Z);
            if(state.getValue(TorsionSpringBlock.AXIS).isHorizontal()) {

                if(getSide() == Direction.UP||getSide() == Direction.DOWN) {
                    if(state.getValue(TorsionSpringBlock.AXIS)== Direction.Axis.X)
                        location = location.add(VecHelper.voxelSpace(0, 0, -3));
                    else if(getSide() == Direction.UP)
                        location = location.add(VecHelper.voxelSpace(3, 0, 0));
                    else
                        location = location.add(VecHelper.voxelSpace(-3, 0, 0));
                }else
                {
                    location = location.add(VecHelper.voxelSpace(0, 3, 0));
                }
            }
            return location;
        }
    }
    private class TorsionSpringValueBoxTransform2 extends ValueBoxTransform.Sided {


        @Override
        protected Vector3d getSouthLocation() {
            return VecHelper.voxelSpace(8, 8f, 14);
        }

        @Override
        protected float getScale() {
            return 0.3f;
        }

        protected ValueBoxTransform getMovementModeSlot() {
            return new DirectionalExtenderScrollOptionSlot((state, d) -> {
                Direction.Axis axis = d.getAxis();


                Direction.Axis shaftAxis = ((IRotate) state.getBlock()).getRotationAxis(state);

                return shaftAxis != axis;
            });
        }
        @Override
        protected boolean isSideActive(BlockState state, Direction direction)
        {
            return !state.getValue(TorsionSpringBlock.AXIS).test(direction);
        }
        @Override
        protected void rotate(BlockState state, MatrixStack ms) {
            float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
            float xRot = getSide() == Direction.UP ? 90 : getSide() == Direction.DOWN ? 270 : 0;
            MatrixTransformStack.of(ms)
                    .rotateY(yRot)
                    .rotateX(xRot);
        }
        @Override
        protected Vector3d getLocalOffset(BlockState state) {
            Vector3d location = getSouthLocation();
            if(state.getValue(TorsionSpringBlock.AXIS).isVertical()) {
                location=location.add(VecHelper.voxelSpace(3, 0, 0));
            }
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.Z);
            if(state.getValue(TorsionSpringBlock.AXIS).isHorizontal()) {

                if(getSide() == Direction.UP||getSide() == Direction.DOWN) {
                    if(state.getValue(TorsionSpringBlock.AXIS)== Direction.Axis.X)
                        location = location.add(VecHelper.voxelSpace(0, 0, 3));
                    else if(getSide() == Direction.UP)
                        location = location.add(VecHelper.voxelSpace(-3, 0, 0));
                    else
                        location = location.add(VecHelper.voxelSpace(3, 0, 0));
                }else
                {
                    location = location.add(VecHelper.voxelSpace(0, -3, 0));
                }
            }
            return location;
        }
    }


    public static int step(ScrollValueBehaviour.StepContext context) {
        int current = context.currentValue;
        int largeStep=10;
        int step = largeStep;

        if (context.shift) {
            step=1;
        }
        int out = (int) (current + (context.forward ? step : -step) == 0 ? step + 1 : step);
        if(!context.shift)
        {

            if(context.forward) {
                out+=current;
                out = (int) Math.floor(out / largeStep) * largeStep;
                out-=current;
            }
            else
            {
                out-=current;
                out = (int)Math.floor(out/largeStep)*largeStep;
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
        return stage ==2 ? 0:stressimpact;
    }
    public void tick() {

        flicker = 0;
        super.tick();
        if(updateNextTick)
        {
            updateNextTick=false;
            stage=2;
            externalChange=false;
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
                externalChange=false;
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
            externalChange=false;
            stage =1;
            currentModifier=0.000000000001f;
            detachKinetics();
            attachKinetics();
            sendData();
        }
        externalChange=true;
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
            externalChange=false;
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


            }


            detachKinetics(getWorld(),worldPosition,true);

            //updateNextTick=true;
            //attachKinetics();
            springbackSpeed = -Math.abs(drivenSpeed)*Math.signum(currentAngle);

            this.sendData();
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
        compound.putBoolean("externalChange", externalChange);
        super.write(compound, clientPacket);
    }

    @Override
    protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
        stage = compound.getInt("stage");
        currentAngle = compound.getFloat("currentAngle");
        currentModifier = compound.getFloat("currentModifier");
        drivenSpeed = compound.getFloat("drivenSpeed");
        externalChange=compound.getBoolean("externalChange");
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
        externalChange=false;
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
                externalChange=false;
                worldIn.getBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 0, TickPriority.EXTREMELY_HIGH);
            }
        }
    }
    public void updateSignal() {
        boolean shouldPower = this.level.hasNeighborSignal(this.worldPosition);
        if (shouldPower != this.powered) {
            this.powered = shouldPower;
            this.sendData();
        }
    }
    public boolean isSource() {
        if(externalChange&&stage==2)
        {
            System.out.println("from isSource");
            externalChange=false;
            //setSpeed(0);
            stage=0;
            //updateGeneratedRotation();

            //stage=2;
            //currentModifier=0;


            updateGeneratedRotation();
            detachKinetics(getWorld(),worldPosition,true);
            //attachKinetics();
            sendData();
            currentModifier=1;

        }
        return getGeneratedSpeed() != 0;
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof KineticTileEntity) {
            externalChange=false;
            KineticTileEntity kte = (KineticTileEntity)te;
            RotationPropagator.handleAdded(worldIn, pos, kte);
        }
    }
    static enum MovementMode implements INamedIconOptions {

        MOVE_PLACE(AllIcons.I_MOVE_PLACE),
        MOVE_PLACE_RETURNED(AllIcons.I_MOVE_PLACE_RETURNED),
        MOVE_NEVER_PLACE(AllIcons.I_MOVE_NEVER_PLACE),

        ;

        private String translationKey;
        private AllIcons icon;

        private MovementMode(AllIcons icon) {
            this.icon = icon;
            translationKey = "contraptions.movement_mode." + Lang.asId(name());
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

    }

}

