package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.blocks.airship_assembler.AirshipAssemblerTileEntity;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.index.CAEntityTypes;
import com.eriksonn.createaeronautics.utils.Matrix3dExtension;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;


public class AirshipContraptionEntity extends AbstractContraptionEntity {

    float time =0;
    Vector3f CurrentAxis=new Vector3f(1,1,1);
    float CurrentAxisAngle = 0;
    public Quaternion quat =Quaternion.ONE;
    public Vector3d velocity;
    public AirshipContraption airshipContraption;
    public int plotId =0;
    PhysicsManager physicsManager;
    public AirshipContraptionEntity(EntityType<?> type, World world) {

        super(type, world);
        physicsManager=new PhysicsManager(this);
        System.out.println("New airship entity");
    }

    public static AirshipContraptionEntity create(World world, AirshipContraption contraption) {
        AirshipContraptionEntity entity = new AirshipContraptionEntity((EntityType) CAEntityTypes.AIRSHIP_CONTRAPTION.get(), world);
        entity.setContraption(contraption);

        //entity.setInitialOrientation(initialOrientation);
        //entity.startAtInitialYaw();
        entity.airshipContraption=contraption;
        AirshipManager.INSTANCE.tryAddEntity(0,entity);
        System.out.println("Airship entity created");
        return entity;

    }

    protected void tickContraption() {
        AirshipAssemblerTileEntity controller = getController();
        airshipContraption=(AirshipContraption) contraption;
        //if(level.isClientSide)
            AirshipManager.INSTANCE.performClientBlockStateChanges(this);
        if(controller!=null)
            controller.attach(this);
        //time++;
        //MatrixStack[] stack = new MatrixStack[1];
        //stack[0]=new MatrixStack();
        //Vector3d axis = new Vector3d(1,1,1);
        //axis.normalize();
        float c = (float)Math.cos(CurrentAxisAngle);
        float s = (float)Math.sin(CurrentAxisAngle);
        CurrentAxis=new Vector3f(c,3,s);
                CurrentAxis=new Vector3f(0,1,0);
        CurrentAxis.normalize();

        //quat=new Quaternion(s*CurrentAxis.x(),s*CurrentAxis.y(),s*CurrentAxis.z(),c);
        CurrentAxisAngle+=0.005f;
        physicsManager.tick();
        //CurrentAxisAngle= (float) (Math.PI*0.125f);
        //this.getContraption().getContraptionWorld().tickBlockEntities();


    }
    protected AirshipAssemblerTileEntity getController() {
        BlockPos controllerPos = AirshipManager.getPlotPosFromId(plotId);
        World w = AirshipDimensionManager.INSTANCE.getWorld();
        if (!w.isLoaded(controllerPos))
            return null;
        TileEntity te = w.getBlockEntity(controllerPos);
        if (!(te instanceof AirshipAssemblerTileEntity))
            return null;
        return (AirshipAssemblerTileEntity) te;
    }


    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        readAdditional(additionalData.readNbt(), true);
    }
    @Override
    protected void readAdditional(CompoundNBT compound, boolean spawnPacket) {
        super.readAdditional(compound, spawnPacket);
        plotId = compound.getInt("PlotId");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
        super.writeAdditional(compound, spawnPacket);
        compound.putInt("PlotId", plotId);
    }
    @Override
    public AirshipRotationState getRotationState() {
        AirshipRotationState crs = new AirshipRotationState();
        crs.matrix=new Matrix3d();
        Vector3d I = PhysicsManager.rotateQuatReverse(new Vector3d(1,0,0),quat);
        Vector3d J = PhysicsManager.rotateQuatReverse(new Vector3d(0,1,0),quat);
        Vector3d K = PhysicsManager.rotateQuatReverse(new Vector3d(0,0,1),quat);
        ((Matrix3dExtension)crs.matrix).createaeronautics$set(I,J,K);
        crs.matrix.transpose();
        return crs;
    }

    public Vector3d reverseRotation(Vector3d localPos, float partialTicks) {
        return PhysicsManager.rotateQuatReverse(localPos,physicsManager.getPartialOrientation(partialTicks));
    }
    public Vector3d applyRotation(Vector3d localPos, float partialTicks) {
        return PhysicsManager.rotateQuat(localPos,physicsManager.getPartialOrientation(partialTicks));
    }
    public Vector3d toGlobalVector(Vector3d localVec, float partialTicks) {
        Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
        localVec = localVec.subtract(rotationOffset);
        localVec = applyRotation(localVec, partialTicks);
        localVec = localVec.add(rotationOffset)
                .add(getAnchorVec());
        return localVec;
    }

    public Vector3d toLocalVector(Vector3d globalVec, float partialTicks) {
        Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
        globalVec = globalVec.subtract(getAnchorVec())
                .subtract(rotationOffset);
        globalVec = reverseRotation(globalVec, partialTicks);
        globalVec = globalVec.add(rotationOffset);
        return globalVec;
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
    public boolean handlePlayerInteraction2(PlayerEntity player, BlockPos localPos, Direction side,
                                           Hand interactionHand) {
        return true;
    }
    public boolean handlePlayerInteraction(PlayerEntity player, BlockPos localPos, Direction side,
                                           Hand interactionHand) {
        int indexOfSeat = contraption.getSeats()
                .indexOf(localPos);
        if(indexOfSeat==-1&&player instanceof ServerPlayerEntity) {
            BlockPos dimensionPos = localPos.offset(AirshipManager.getPlotPosFromId(plotId));
            World worldIn = AirshipDimensionManager.INSTANCE.getWorld();
            BlockState state = worldIn.getBlockState(dimensionPos);

            state.getBlock().use(state, worldIn, dimensionPos, player, interactionHand, null);
            return true;
        }
        // Eject potential existing passenger
        Entity toDismount = null;
        for (Map.Entry<UUID, Integer> entry : contraption.getSeatMapping()
                .entrySet()) {
            if (entry.getValue() != indexOfSeat)
                continue;
            for (Entity entity : getPassengers()) {
                if (!entry.getKey()
                        .equals(entity.getUUID()))
                    continue;
                if (entity instanceof PlayerEntity)
                    return false;
                toDismount = entity;
            }
        }

        if (toDismount != null && !level.isClientSide) {
            Vector3d transformedVector = getPassengerPosition(toDismount, 1);
            toDismount.stopRiding();
            if (transformedVector != null)
                toDismount.teleportTo(transformedVector.x, transformedVector.y, transformedVector.z);
        }

        if (level.isClientSide)
            return true;
        addSittingPassenger(player, indexOfSeat);
        return true;
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
        Quaternion Q=physicsManager.getPartialOrientation(partialTicks);
        Q.conj();
        for(var8 = 0; var8 < var7; ++var8) {
            MatrixStack stack = var6[var8];
            stack.translate(0.5,0.5,0.5);
            stack.mulPose(Q);
            stack.translate(-0.5,-0.5,-0.5);
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
    public static class AirshipRotationState extends ContraptionRotationState
    {
        public static final ContraptionRotationState NONE = new ContraptionRotationState();

        float xRotation = 0;
        float yRotation = 0;
        float zRotation = 0;
        float secondYRotation = 0;
        Matrix3d matrix;

        public Matrix3d asMatrix() {
            if (matrix != null)
                return matrix;

            matrix = new Matrix3d().asIdentity();
            if (xRotation != 0)
                matrix.multiply(new Matrix3d().asXRotation(AngleHelper.rad(-xRotation)));
            if (yRotation != 0)
                matrix.multiply(new Matrix3d().asYRotation(AngleHelper.rad(yRotation)));
            if (zRotation != 0)
                matrix.multiply(new Matrix3d().asZRotation(AngleHelper.rad(-zRotation)));
            return matrix;
        }

        public boolean hasVerticalRotation() {
            return true;
        }

        public float getYawOffset() {
            return secondYRotation;
        }
    }
}
