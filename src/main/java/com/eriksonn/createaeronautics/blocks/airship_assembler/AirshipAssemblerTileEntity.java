package com.eriksonn.createaeronautics.blocks.airship_assembler;
import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipContraption;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.dimension.ClientDimensionManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class AirshipAssemblerTileEntity extends SmartTileEntity implements IDisplayAssemblyExceptions {
    public boolean running=false;
    protected AirshipContraptionEntity movedContraption;
    protected boolean assembleNextTick;
    protected AssemblyException lastException;
    float time=0;


    public AirshipAssemblerTileEntity(TileEntityType<?> typeIn) {

        super(typeIn);
        this.setLazyTickRate(3);
    }
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        //super.addBehaviours(behaviours);
    }
    public AssemblyException getLastAssemblyException() {
        return this.lastException;
    }
    public void assemble() {
        if (this.level.getBlockState(this.worldPosition).getBlock() instanceof AirshipAssemblerBlock) {
            //Direction direction = (Direction)this.getBlockState().getValue(BlockStateProperties.FACING);
            AirshipContraption contraption = new AirshipContraption();

            try {
                if (!contraption.assemble(this.level, this.worldPosition)) {
                    return;
                }

                this.lastException = null;
            } catch (AssemblyException var4) {
                this.lastException = var4;
                this.sendData();
                return;
            }
            AirshipDimensionManager manager = AirshipDimensionManager.INSTANCE;
            BlockPos targetSize = new BlockPos(contraption.bounds.maxX,contraption.bounds.maxY,contraption.bounds.maxZ);
            //manager.getWorldData();
            //AirshipDimensionPlot plot = manager.allocatePlot(targetSize);
            ServerWorld airshipWorld = manager.getWorld();

            //make sure that the client world exists
            //ClientDimensionManager.getWorld();

            //contraption.storageWorld=airshipWorld;


            contraption.removeBlocksFromWorld(this.level, BlockPos.ZERO);
            this.movedContraption = AirshipContraptionEntity.create(this.level, contraption);
            BlockPos anchor = this.worldPosition;
            this.movedContraption.physicsManager.tryInit();

            this.movedContraption.setPos(
                    (double)anchor.getX()+this.movedContraption.centerOfMassOffset.x,
                    (double)anchor.getY()+this.movedContraption.centerOfMassOffset.y,
                    (double)anchor.getZ()+this.movedContraption.centerOfMassOffset.z);
            this.level.addFreshEntity(this.movedContraption);
            AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(this.level, this.worldPosition);
            this.running = true;
            //contraption.startMoving(this.level);

            //airshipWorld.getChunk(this.worldPosition).setBlockState(this.worldPosition,this.getBlockState(),true);
            //airshipWorld.getChunk(this.worldPosition).setBlockEntity(this.worldPosition,this);
            //airshipWorld.setBlockAndUpdate(this.worldPosition, this.getBlockState());
            //airshipWorld.setBlockEntity(this.worldPosition,this);
            //this.level.removeBlock(this.worldPosition,false);
            //this.level.removeBlockEntity(this.worldPosition);
            contraption.addBlocksToWorld(airshipWorld,new StructureTransform(AirshipManager.getPlotPosFromId(0), 0.0F, 0, 0.0F));
            //this.level.removeBlockEntity(this.worldPosition);
            //airshipWorld.addBlockEntity(this);
            this.sendData();
        }
    }
    public void tick() {
        super.tick();



        if (!this.level.isClientSide && this.assembleNextTick) {
            this.assembleNextTick = false;
            if (!this.running) {
                //System.out.println("assembly start");
                this.assemble();

                //System.out.println("assembly complete");
            }
            time=0;
        }
        if(this.movedContraption!=null)
        {

time++;
float angle=time*5.5f;
MatrixStack[] stack = new MatrixStack[1];
stack[0]=new MatrixStack();
Vector3d axis = new Vector3d(1,1,1);
axis.normalize();
Quaternion Q = new Quaternion((float)Math.cos(angle),(float)(Math.sin(angle)*axis.x),(float)(Math.sin(angle)*axis.y),(float)(Math.sin(angle)*axis.z));
stack[0].mulPose(Q);

//this.movedContraption.doLocalTransforms(AnimationTickHolder.getPartialTicks(),stack);

            //this.movedContraption.applyRotation(axis,time);
        }

        this.running = this.level==AirshipDimensionManager.INSTANCE.getWorld();

    }
    public void lazyTick() {
        super.lazyTick();
        if (this.movedContraption != null && !this.level.isClientSide) {
            this.sendData();
        }

    }
    public void attach(AirshipContraptionEntity contraption) {
        this.movedContraption = contraption;
        /*BlockState blockState = this.getBlockState();
        if (contraption.getContraption() instanceof BearingContraption) {
            if (blockState.hasProperty(BlockStateProperties.FACING)) {
                this.movedContraption = contraption;
                this.setChanged();
                BlockPos anchor = this.worldPosition.relative((Direction)blockState.getValue(BlockStateProperties.FACING));
                this.movedContraption.setPos((double)anchor.getX(), (double)anchor.getY(), (double)anchor.getZ());
                if (!this.level.isClientSide) {
                    this.running = true;
                    this.sendData();
                }

            }
        }*/
    }
    public void disassemble() {
        if (this.running || this.movedContraption != null) {

            int plotId=AirshipManager.getIdFromPlotPos(this.worldPosition);


            if (this.movedContraption != null) {
                this.movedContraption.disassemble();

                AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(this.level, this.worldPosition);
            }
            //AirshipManager.INSTANCE.removePlot(plotId);
            this.movedContraption = null;
            this.running = false;
            this.assembleNextTick = false;
            this.sendData();
        }
    }
    public void onStall() {
        if (!this.level.isClientSide) {
            this.sendData();
        }

    }
    public void collided() {
    }
    public BlockPos getBlockPosition() {
        return this.worldPosition;
    }
    public boolean isValid() {
        return !this.isRemoved();
    }
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        return this.movedContraption == contraption;
    }
}
