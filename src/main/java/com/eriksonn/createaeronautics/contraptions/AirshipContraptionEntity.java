package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.blocks.airship_assembler.AirshipAssemblerTileEntity;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.dimension.AirshipWorldData;
import com.eriksonn.createaeronautics.index.CAEntityTypes;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;

public class AirshipContraptionEntity extends AbstractContraptionEntity {

    float time =0;
    Vector3f CurrentAxis=new Vector3f(1,1,1);
    float CurrentAxisAngle = 0;
    Quaternion Quat=Quaternion.ONE;
    public AirshipContraption airshipContraption;
    public int id=0;
    public AirshipContraptionEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public static AirshipContraptionEntity create(World world, AirshipContraption contraption) {
        AirshipContraptionEntity entity = new AirshipContraptionEntity((EntityType) CAEntityTypes.AIRSHIP_CONTRAPTION.get(), world);
        entity.setContraption(contraption);
        //entity.setInitialOrientation(initialOrientation);
        //entity.startAtInitialYaw();
        entity.airshipContraption=contraption;
        AirshipManager.INSTANCE.tryAddEntity(0,entity);
        return entity;

    }

    protected void tickContraption() {
        airshipContraption=(AirshipContraption) contraption;
        if(level.isClientSide)
            AirshipManager.INSTANCE.performClientBlockStateChanges(this);
        time++;
        //MatrixStack[] stack = new MatrixStack[1];
        //stack[0]=new MatrixStack();
        //Vector3d axis = new Vector3d(1,1,1);
        //axis.normalize();
float c = (float)Math.cos(CurrentAxisAngle*1.2);
float s = (float)Math.sin(CurrentAxisAngle*1.2);
CurrentAxis=new Vector3f(c,s,2);
CurrentAxis.normalize();
Quat=new Quaternion(s*CurrentAxis.x(),s*CurrentAxis.y(),s*CurrentAxis.z(),c);
CurrentAxisAngle+=0.02f;
        //this.getContraption().getContraptionWorld().tickBlockEntities();


    }


    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        readAdditional(additionalData.readNbt(), true);
    }
    @Override
    protected void readAdditional(CompoundNBT compound, boolean spawnPacket) {
        super.readAdditional(compound, spawnPacket);
        id = compound.getInt("PlotId");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
        super.writeAdditional(compound, spawnPacket);
        compound.putInt("PlotId", id);
    }
    public ContraptionRotationState getRotationState() {
        ContraptionRotationState crs = new ContraptionRotationState();
        crs.asMatrix();

        float yawOffset = this.getYawOffset();

        //crs.zRotation = this.pitch;
        //crs.yRotation = -this.yaw + yawOffset;
        //if (this.pitch != 0.0F && this.yaw != 0.0F) {
        //    crs.secondYRotation = -this.yaw;
        //    crs.yRotation = yawOffset;
        //}
        return crs;
    }
    public Vector3d applyRotation(Vector3d localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, 0.0, Direction.Axis.Y);
        localPos = VecHelper.rotate(localPos, (double)this.getViewXRot(partialTicks), Direction.Axis.Z);
        localPos = VecHelper.rotate(localPos, (double)this.getViewYRot(partialTicks), Direction.Axis.Y);
        return localPos;
    }
    public Vector3d reverseRotation(Vector3d localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, (double)(-this.getViewYRot(partialTicks)), Direction.Axis.Y);
        localPos = VecHelper.rotate(localPos, (double)(-this.getViewXRot(partialTicks)), Direction.Axis.Z);
        localPos = VecHelper.rotate(localPos, (double)(-0.0), Direction.Axis.Y);
        return localPos;
    }
    protected StructureTransform makeStructureTransform() {
        BlockPos offset = new BlockPos(this.getAnchorVec().add(0.0D, 0.0D, 0.0D));
        return new StructureTransform(offset, 0.0F, 0, 0.0F);
    }
    protected float getStalledAngle() {
        return 0.0f;
    }
    protected void handleStallInformation(float x, float y, float z, float angle) {

    }
    @OnlyIn(Dist.CLIENT)
    public void doLocalTransforms(float partialTicks, MatrixStack[] matrixStacks) {
        float angleInitialYaw = 0.0f;
        float angleYaw = this.getViewYRot(partialTicks);
        float anglePitch = this.getViewXRot(partialTicks);
        //angleYaw=anglePitch=0;
        MatrixStack[] var6 = matrixStacks;
        int var7 = matrixStacks.length;

        int var8;
        for(var8 = 0; var8 < var7; ++var8) {
            MatrixStack stack = var6[var8];
            stack.mulPose(Quat);
            //stack.translate(-0.5D, 0.0D, -0.5D);
        }

        MatrixStack[] var12 = matrixStacks;
        var8 = matrixStacks.length;
        //Quaternion conj = currentQuaternion.copy();
        //conj.conj();
        for(int var13 = 0; var13 < var8; ++var13) {
            MatrixStack stack = var12[var13];

            //MatrixTransformStack.of(stack).nudge(this.getId()).centre().rotateY((double)angleYaw).rotateZ((double)anglePitch).rotateY((double)angleInitialYaw).multiply(CurrentAxis,Math.toDegrees(CurrentAxisAngle)).unCentre();
            MatrixTransformStack.of(stack).nudge(this.getId()).centre().rotateY((double)angleYaw).rotateZ((double)anglePitch).rotateY((double)angleInitialYaw).unCentre();
        }

    }
}
