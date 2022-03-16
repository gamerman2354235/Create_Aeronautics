package com.eriksonn.createaeronautics.blocks.propeller_bearing;

import com.eriksonn.createaeronautics.blocks.stationary_potato_cannon.StationaryPotatoCannonTileEntity;
import com.eriksonn.createaeronautics.particle.PropellerAirParticleData;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;

import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropellerBearingTileEntity extends MechanicalBearingTileEntity{
    public ScrollOptionBehaviour<PropellerBearingTileEntity.RotationDirection> movementDirection;
    protected float lastGeneratedSpeed;
    public List<BlockPos> sailPositions;
    float rotationSpeed=0;
    float disassemblyTimer;
    boolean disassemblySlowdown=false;

    public PropellerBearingTileEntity(TileEntityType<? extends MechanicalBearingTileEntity> type) {
        super(type);
        sailPositions=new ArrayList<>();
    }
    @Override
    public float calculateStressApplied() {
        if (!running)
            return 0;
        int sails=0;
        if (movedContraption != null) {
            sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks();
        }
        sails=Math.max(sails,2);
        return sails*2f;
    }


    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        movementMode.setValue(2);
        behaviours.remove(movementMode);
        movementDirection = new ScrollOptionBehaviour<>(PropellerBearingTileEntity.RotationDirection.class,
                Lang.translate("contraptions.windmill.rotation_direction"), this, getMovementModeSlot());
        movementDirection.requiresWrench();
        movementDirection.withCallback($ -> onDirectionChanged());
        behaviours.add(movementDirection);
    }

    private void onDirectionChanged() {
        BlockState state = getBlockState();
        PropellerBearingBlock.Direction previouslyPowered = state.getValue(PropellerBearingBlock.DIRECTION);
        if (previouslyPowered == PropellerBearingBlock.Direction.PULL)
            level.setBlock(getBlockPos(), state.cycle(PropellerBearingBlock.DIRECTION), 2);
        if (!running)
            return;
        if (!level.isClientSide)
            updateGeneratedRotation();
    }

    @Override
    public void write(CompoundNBT compound, boolean clientPacket) {
        compound.putFloat("LastGenerated", lastGeneratedSpeed);
        compound.putFloat("RotationSpeed", rotationSpeed);
        super.write(compound, clientPacket);
    }

    @Override
    protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
        if (!wasMoved)
            lastGeneratedSpeed = compound.getFloat("LastGenerated");
        rotationSpeed = compound.getFloat("RotationSpeed");
        super.fromTag(state, compound, clientPacket);
    }

    @Override
    public boolean isWoodenTop() {
        return false;
    }
    public float getAngularSpeed() {
        float speed = rotationSpeed;
        if (level.isClientSide) {
            speed *= ServerSpeedProvider.get();
            //speed += clientAngleDiff / 3f;
        }
        return speed;
    }
    @Override
    public void tick() {
        super.tick();

        if(disassemblySlowdown)
            updateSlowdownSpeed();
        else
            updateRotationSpeed();

        if (level.isClientSide) {
            //tickRotation();
            spawnParticles();
            return;
        }
        if(speed!=0)
            lastGeneratedSpeed=speed;
        if((lastGeneratedSpeed<0 && movementDirection.getValue()==0) || (lastGeneratedSpeed>0 && movementDirection.getValue()!=0))
        {
            setBlockDirection(PropellerBearingBlock.Direction.PULL);
        }else
        {
            setBlockDirection(PropellerBearingBlock.Direction.PUSH);
        }
    }
    void updateRotationSpeed()
    {
        float nextSpeed = convertToAngular(getSpeed());
        if (getSpeed() == 0)
            nextSpeed = 0;
        if(sailPositions.size()>0) {
            float lerpAmount = 0.7f / sailPositions.size();
            rotationSpeed = MathHelper.lerp(lerpAmount,rotationSpeed, nextSpeed);
        }else
        {
            rotationSpeed=nextSpeed;
        }
    }
    void updateSlowdownSpeed()
    {
        disassemblyTimer--;
        if(disassemblyTimer==0) {
            if(!level.isClientSide)
                disassemble();
            disassemblySlowdown=false;
            return;
        }

        // the angle it will end up at if slowing down at a constant rate
        float currentStoppingPoint = (angle + rotationSpeed*disassemblyTimer*0.5f);

        // the closest grid-aligned angle to currentStoppingPoint
        float optimalStoppingPoint = 90f*Math.round(currentStoppingPoint/90f);

        // Q is an inverse-lerp that solves this equation:
        // optimalStoppingPoint = lerp(currentStoppingPoint,angle,Q)
        float Q = (optimalStoppingPoint-currentStoppingPoint)/(angle-currentStoppingPoint);

        rotationSpeed *= (1f - 3f*Q/disassemblyTimer)*(1f - 1f/disassemblyTimer);
    }
    @Override
    public void attach(ControlledContraptionEntity contraption)
    {
        //rotationSpeed=0f;
        super.attach(contraption);
        findSails();
    }
    @Override
    public void assemble()
    {
        rotationSpeed=0;
        super.assemble();
        findSails();
    }

    @Override
    public void disassemble()
    {
        super.disassemble();
    }

    public void setAssembleNextTick(boolean value) {
        assembleNextTick = value;
    }

    public void startDisassemblySlowdown()
    {
        if(!disassemblySlowdown) {
            disassemblySlowdown = true;
            disassemblyTimer = 20;
        }
    }
    void findSails()
    {
        sailPositions =new ArrayList<>();
        if(movedContraption!=null) {
            Map<BlockPos, Template.BlockInfo> Blocks = ((BearingContraption) movedContraption.getContraption()).getBlocks();
            for (Map.Entry<BlockPos, Template.BlockInfo> entry : Blocks.entrySet()) {
                if (AllTags.AllBlockTags.WINDMILL_SAILS.matches(entry.getValue().state)) {
                    sailPositions.add(entry.getKey());
                }
            }
        }
    }

    public void spawnParticles()
    {
        if(getSpeed()!=0 && movedContraption!=null &&isRunning()) {
            World world = getLevel();
            Direction direction = getBlockState().getValue(BlockStateProperties.FACING);
            Vector3f speed = new Vector3f(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());

            float directionScale=1f;

            if((rotationSpeed<0 && movementDirection.getValue()==0) || (rotationSpeed>0 && movementDirection.getValue()!=0)) {

                directionScale*=-1;
                //direction = direction.getOpposite();
            }

            float offset = 1.0f+directionScale*0.5f;
            float speedScale = 0.2f*directionScale*Math.abs(rotationSpeed)/5f;
            float particleCount = 0.1f*sailPositions.size()*Math.abs(rotationSpeed)/5f;

            particleCount+=Create.RANDOM.nextFloat()-1.0f;
            for (int i=0;i<particleCount;i++)
            {
                BlockPos sailPos = sailPositions.get(Create.RANDOM.nextInt(sailPositions.size()));
                Vector3d floatPos = new Vector3d(sailPos.getX(),sailPos.getY(),sailPos.getZ());
                floatPos=movedContraption.applyRotation(floatPos,0);

                Vector3d pos = VecHelper.getCenterOf(this.worldPosition)
                        .add(Vector3d.atLowerCornerOf(direction.getNormal())
                                .scale(offset))
                                .add(floatPos);


                world.addParticle(new PropellerAirParticleData(this.worldPosition), pos.x, pos.y, pos.z, speed.x() * speedScale, speed.y() * speedScale, speed.z() * speedScale);
            }
        }
    }

    public PropellerBearingBlock.Direction getDirectionFromBlock() {
        return PropellerBearingBlock.getDirectionof(getBlockState());
    }
    protected void setBlockDirection(PropellerBearingBlock.Direction direction) {
        PropellerBearingBlock.Direction inBlockState = getDirectionFromBlock();
        if (inBlockState == direction)
            return;
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(PropellerBearingBlock.DIRECTION, direction));
        notifyUpdate();
    }

    static enum RotationDirection implements INamedIconOptions {

        CLOCKWISE(AllIcons.I_REFRESH), COUNTER_CLOCKWISE(AllIcons.I_ROTATE_CCW),

        ;

        private String translationKey;
        private AllIcons icon;

        private RotationDirection(AllIcons icon) {
            this.icon = icon;
            translationKey = "generic." + Lang.asId(name());
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
