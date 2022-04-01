package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.blocks.airship_assembler.AirshipAssemblerTileEntity;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.index.CAEntityTypes;
import com.eriksonn.createaeronautics.mixins.ContraptionHolderAccessor;
import com.eriksonn.createaeronautics.mixins.ControlledContraptionEntityMixin;
import com.eriksonn.createaeronautics.network.NetworkMain;
import com.eriksonn.createaeronautics.network.packet.*;
import com.eriksonn.createaeronautics.physics.SimulatedContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.SubcontraptionRigidbody;
import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;
import com.eriksonn.createaeronautics.physics.collision.shape.MeshCollisionShape;
import com.eriksonn.createaeronautics.utils.AbstractContraptionEntityExtension;
import com.eriksonn.createaeronautics.utils.Matrix3dExtension;
import com.eriksonn.createaeronautics.world.FakeAirshipClientWorld;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.*;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;


public class AirshipContraptionEntity extends AbstractContraptionEntity {

    float time = 0;

    public Quaternion quat = Quaternion.ONE;
    public Vector3d velocity;
    public AirshipContraption airshipContraption;
    public int plotId = 0;
    public SimulatedContraptionRigidbody simulatedRigidbody;

    public Map<UUID, ControlledContraptionEntity> subContraptions = new HashMap<>();
    public Vector3d centerOfMassOffset = Vector3d.ZERO;
    public static final DataParameter<CompoundNBT> physicsDataAccessor = EntityDataManager.defineId(AirshipContraptionEntity.class, DataSerializers.COMPOUND_TAG);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(physicsDataAccessor, new CompoundNBT());
    }

    public AirshipContraptionEntity(EntityType<?> type, World world) {
        super(type, world);
        simulatedRigidbody = new SimulatedContraptionRigidbody(this);

        // testing
//        this.simulatedRigidbody.angularMomentum = new Vector3d(0, 0, 40);

        System.out.println("New airship entity");
    }

    public static AirshipContraptionEntity create(World world, AirshipContraption contraption) {
        AirshipContraptionEntity entity = new AirshipContraptionEntity((EntityType) CAEntityTypes.AIRSHIP_CONTRAPTION.get(), world);
        entity.setContraption(contraption);

        entity.airshipContraption = contraption;
        AirshipManager.INSTANCE.tryAddEntity(AirshipManager.INSTANCE.getNextId(), entity);

        return entity;

    }

    // Whether or not this contraption has been airshipInitialized
    public boolean airshipInitialized = false;

    public boolean invalid = false;
    public boolean syncNextTick = false;

    HashSet<BlockPos> blocksToUpdate = new HashSet<>();

    @Override
    public void tickContraption() {
        AirshipAssemblerTileEntity controller = getController();
        airshipContraption = (AirshipContraption) contraption;

        if (controller != null)
            controller.attach(this);

        simulatedRigidbody.tick();

        if (!airshipInitialized) {
            initFakeClientWorld();
        }

        if (level.isClientSide) {
            profiler.startTick();
            fakeClientWorld.tick(() -> true);

            for (ControlledContraptionEntity contraptionEntity : subContraptions.values()) {
                contraptionEntity.tick();
            }
            fakeClientWorld.tickEntities();

            fakeClientWorld.tickBlockEntities();
            profiler.endTick();

            if (invalid) {
                ContraptionRenderDispatcher.invalidate(airshipContraption);
                invalid = false;
            }
        }

        if (!airshipInitialized) {
            airshipInitialized = true;
            syncNextTick = true;
        }

        if (syncNextTick) {
            syncPacket();
            syncPacket();
            syncNextTick = false;
        }

        contraption.getContraptionWorld().tickBlockEntities();

        if (!level.isClientSide) {
            serverUpdate();
        }
        //Vector3d startVector=new Vector3d(1,5,7);
        //Vector3d intermediateVector=simulatedRigidbody.multiplyInertia(startVector);
        //Vector3d endVector=simulatedRigidbody.multiplyInertiaInverse(intermediateVector);
        //for(Map.Entry<UUID, SubcontraptionRigidbody> entry : simulatedRigidbody.subcontraptionRigidbodyMap.entrySet())
        //{
        //    SubcontraptionRigidbody rigidbody = entry.getValue();
//        x


        //}

//        if(level.isClientSide)
        


//        Vector3d particlePos = simulatedRigidbody.toGlobal(simulatedRigidbody.toLocal(simulatedRigidbody.toGlobal(new Vector3d(1,1,1))));
//        level.addParticle(new RedstoneParticleData(1,1,1,1),particlePos.x,particlePos.y,particlePos.z,0,0,0);

    }

    public BlockPos getPlotPos() {
        return AirshipManager.getPlotPosFromId(plotId);
    }



    private void putDoubleArray(CompoundNBT tag, String key, double[] array) {
        ListNBT list = new ListNBT();
        for (double d : array) {
            list.add(DoubleNBT.valueOf(d));
        }
        tag.put(key, list);
    }

    private double[] readDoubleArray(CompoundNBT tag, String key) {
        INBT[] boxed = tag.getList(key, Constants.NBT.TAG_DOUBLE).toArray(new INBT[0]);
        double[] unboxed = new double[boxed.length];
        for (int i = 0; i < boxed.length; i++) {
            unboxed[i] = ((DoubleNBT) boxed[i]).getAsDouble();
        }
        return unboxed;
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> pKey) {
        if (pKey.equals(physicsDataAccessor)) {
            CompoundNBT tag = this.entityData.get((DataParameter<CompoundNBT>) pKey);

            simulatedRigidbody.momentum = simulatedRigidbody.arrayToVec(readDoubleArray(tag, "momentum"));
            simulatedRigidbody.angularMomentum = simulatedRigidbody.arrayToVec(readDoubleArray(tag, "angularMomentum"));
            simulatedRigidbody.orientation = simulatedRigidbody.arrayToQuat(readDoubleArray(tag, "orientation"));
        }
    }

    @Override
    public void onRemovedFromWorld() {
        subContraptions.forEach((uuid, contraptionEntity) -> {
            contraptionEntity.getContraption().addBlocksToWorld(contraptionEntity.level, ((ControlledContraptionEntityMixin)contraptionEntity).invokeMakeStructureTransform());
            contraptionEntity.remove();
            serverDestroySubContraption(contraptionEntity);
        });
        super.onRemovedFromWorld();
    }

    public FakeAirshipClientWorld fakeClientWorld;

    public void serverUpdate() {
        // stcDestroySubContraption and remove from the hashmap all subcontraptions that arent alive
        Set<UUID> keyset = new HashSet<>(subContraptions.keySet());

        for (UUID uuid : keyset) {
            ControlledContraptionEntity subContraption = subContraptions.get(uuid);
            if (!subContraption.isAlive()) {
                subContraptions.remove(uuid);
                simulatedRigidbody.removeSubContraption(uuid);
                serverDestroySubContraption(subContraption);
            } else {
                serverUpdateSubContraption(subContraption);
            }
        }

        CompoundNBT tag = new CompoundNBT();
        putDoubleArray(tag, "angularMomentum", simulatedRigidbody.vecToArray(simulatedRigidbody.angularMomentum));
        putDoubleArray(tag, "momentum", simulatedRigidbody.vecToArray(simulatedRigidbody.momentum));
        putDoubleArray(tag, "orientation", simulatedRigidbody.quatToArray(simulatedRigidbody.orientation));
        this.entityData.set(physicsDataAccessor, tag);

        // for everything in the hashmap, update the client
        for (BlockPos pos : blocksToUpdate) {
            stcHandleBlockUpdate(pos);
        }

        blocksToUpdate.clear();
    }

    private void serverDestroySubContraption(ControlledContraptionEntity subContraption) {
        notifyClients(new AirshipDestroySubcontraptionPacket(plotId, subContraption.getUUID()));
    }

    public void serverUpdateSubContraption(ControlledContraptionEntity subContraption) {
        notifyClients(new AirshipUpdateSubcontraptionPacket(plotId, subContraption.serializeNBT(), subContraption.getUUID()));
    }

    public void initFakeClientWorld() {
        if (level.isClientSide) {
            profiler = new Profiler(() -> 0, () -> 0, false);
            RegistryKey<World> dimension = level.dimension();
            DimensionType dimensionType = level.dimensionType();
            ClientWorld.ClientWorldInfo clientWorldInfo = new ClientWorld.ClientWorldInfo(Difficulty.PEACEFUL, false, true);
            fakeClientWorld = new FakeAirshipClientWorld(
                    this,
                    Minecraft.getInstance().getConnection(),
                    clientWorldInfo,
                    dimension,
                    dimensionType,
                    0, () -> profiler,
                    null, false, 0
            );
            AirshipManager.INSTANCE.AllClientAirships.put(plotId, this);

            airshipContraption.maybeInstancedTileEntities.clear();
            airshipContraption.specialRenderedTileEntities.clear();
            airshipContraption.presentTileEntities.clear();
        }
    }

    public void syncPacket() {
        if (!level.isClientSide) {

            // plot pos
            BlockPos plotPos = getPlotPos();

            // for every block
            for (Map.Entry<BlockPos, Template.BlockInfo> blockStateEntry : contraption.getBlocks().entrySet()) {
                int x = blockStateEntry.getKey().getX() + plotPos.getX();
                int y = blockStateEntry.getKey().getY() + plotPos.getY();
                int z = blockStateEntry.getKey().getZ() + plotPos.getZ();

                BlockPos pos = new BlockPos(x, y, z);
                ServerWorld serverLevel = AirshipDimensionManager.INSTANCE.getWorld();

                BlockState state = serverLevel.getBlockState(pos.offset(getPlotPos()));
                if (!state.getBlock().is(Blocks.AIR)) {
                    TileEntity te = serverLevel.getBlockEntity(pos.offset(getPlotPos()));
                    if (te instanceof ITickableTileEntity) {
                        ((ITickableTileEntity) te).tick();
                    }
                    stcQueueBlockUpdate(pos);
                }
            }
        }
    }

    public void stcQueueBlockUpdate(BlockPos localPos) {
        blocksToUpdate.add(localPos);
    }

    public void stcHandleBlockUpdate(BlockPos localPos) {

        if (!airshipInitialized) return;
        BlockPos plotPos = getPlotPos();

        // Server level!
        ServerWorld serverLevel = AirshipDimensionManager.INSTANCE.getWorld();

        // get block state
        BlockPos pos = plotPos.offset(localPos);
        BlockState state = serverLevel.getBlockState(pos);

        CompoundNBT thisBlockNBT = new CompoundNBT();

        thisBlockNBT.putInt("x", pos.getX() - plotPos.getX());
        thisBlockNBT.putInt("y", pos.getY());
        thisBlockNBT.putInt("z", pos.getZ() - plotPos.getZ());
        thisBlockNBT.put("state", NBTUtil.writeBlockState(state));

        TileEntity blockEntity = state.hasTileEntity() ? serverLevel.getBlockEntity(pos) : null;
        SUpdateTileEntityPacket updatePacket = null;
        if (blockEntity != null) {
            thisBlockNBT.put("be", blockEntity.serializeNBT());
            updatePacket = blockEntity.getUpdatePacket();

            addTileData(blockEntity, pos.offset(-plotPos.getX(), -plotPos.getY(), -plotPos.getZ()), state);
            handleControllingSubcontraption(blockEntity, pos);
        }

        thisBlockNBT.putInt("plotId", plotId);

        AirshipContraptionBlockUpdatePacket packet = new AirshipContraptionBlockUpdatePacket(thisBlockNBT);
        notifyClients(packet);
        if(updatePacket != null) {
            notifyClients(new AirshipBEUpdatePacket(updatePacket.getType(), updatePacket.getTag(), new BlockPos(pos.getX() - plotPos.getX(), pos.getY(),pos.getZ() - plotPos.getZ()), plotId));
        }

        airshipContraption.setBlockState(localPos, state, blockEntity);
    }

    private void notifyClients(Object packet) {
        NetworkMain.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
                new BlockPos(position())
        )), packet);
    }

    public AirshipAssemblerTileEntity getController() {
        BlockPos controllerPos = getPlotPos();
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
        simulatedRigidbody.globalVelocity = simulatedRigidbody.arrayToVec(readDoubleArray(compound, "velocity"));
        //simulatedRigidbody.angularVelocity = simulatedRigidbody.arrayToVec(readDoubleArray(compound, "angularVelocity"));
        simulatedRigidbody.orientation = simulatedRigidbody.arrayToQuat(readDoubleArray(compound, "orientation"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
        super.writeAdditional(compound, spawnPacket);
        compound.putInt("PlotId", plotId);
        putDoubleArray(compound, "velocity", simulatedRigidbody.vecToArray(simulatedRigidbody.globalVelocity));
        //putDoubleArray(compound, "angularVelocity", simulatedRigidbody.vecToArray(simulatedRigidbody.angularVelocity));
        putDoubleArray(compound, "orientation", simulatedRigidbody.quatToArray(simulatedRigidbody.orientation));
    }

    @Override
    public AirshipRotationState getRotationState() {
        AirshipRotationState crs = new AirshipRotationState();
        crs.matrix = new Matrix3d();
        Vector3d I = SimulatedContraptionRigidbody.rotateQuatReverse(new Vector3d(1, 0, 0), quat);
        Vector3d J = SimulatedContraptionRigidbody.rotateQuatReverse(new Vector3d(0, 1, 0), quat);
        Vector3d K = SimulatedContraptionRigidbody.rotateQuatReverse(new Vector3d(0, 0, 1), quat);
        ((Matrix3dExtension) crs.matrix).createaeronautics$set(I, J, K);
        crs.matrix.transpose();
        return crs;
    }

    public Vector3d reverseRotation(Vector3d localPos, float partialTicks) {
        return SimulatedContraptionRigidbody.rotateQuatReverse(localPos, simulatedRigidbody.getPartialOrientation(partialTicks));
    }

    public Vector3d applyRotation(Vector3d localPos, float partialTicks) {
        return SimulatedContraptionRigidbody.rotateQuat(localPos, simulatedRigidbody.getPartialOrientation(partialTicks));
    }

    public Vector3d toGlobalVector(Vector3d localVec, float partialTicks) {
        double x = MathHelper.lerp(partialTicks, xOld, getX());
        double y = MathHelper.lerp(partialTicks, yOld, getY());
        double z = MathHelper.lerp(partialTicks, zOld, getZ());
        Vector3d anchorVec = new Vector3d(x, y, z);

        Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
        localVec = localVec.subtract(rotationOffset).subtract(centerOfMassOffset);
        //localVec = localVec.subtract(rotationOffset);
        localVec = applyRotation(localVec, partialTicks);
        localVec = localVec.add(rotationOffset)
                .add(anchorVec);
        return localVec;
    }

    public Vector3d toLocalVector(Vector3d globalVec, float partialTicks) {
        Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
        globalVec = globalVec.subtract(getAnchorVec())
                .subtract(rotationOffset);
        globalVec = reverseRotation(globalVec, partialTicks);
        globalVec = globalVec.add(rotationOffset);
        //return globalVec;
        return globalVec.add(centerOfMassOffset);
    }

    protected StructureTransform makeStructureTransform() {
        BlockPos offset = new BlockPos(this.getAnchorVec().subtract(centerOfMassOffset));
        return new StructureTransform(offset, 0.0F, 0, 0.0F);
    }

    @Override
    public Vector3d getAnchorVec() {
        return position();
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
        if (indexOfSeat == -1 && player instanceof ServerPlayerEntity) {
            BlockPos dimensionPos = localPos.offset(getPlotPos());
            World worldIn = AirshipDimensionManager.INSTANCE.getWorld();
            BlockState state = worldIn.getBlockState(dimensionPos);

            try {
                state.getBlock().use(state, worldIn, dimensionPos, player, interactionHand, null);
            } catch (Exception e) {

            }
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
        Quaternion Q = simulatedRigidbody.getPartialOrientation(partialTicks);
        Vector3d partialPosition = getPartialPosition(partialTicks);
        Vector3d position = position();
        Q.conj();
        for (var8 = 0; var8 < var7; ++var8) {
            MatrixStack stack = var6[var8];
            stack.translate(0.5, 0.5, 0.5);
            stack.mulPose(Q);
            stack.translate(-centerOfMassOffset.x, -centerOfMassOffset.y, -centerOfMassOffset.z);
            stack.translate(-0.5, -0.5, -0.5);
//            stack.translate(partialPosition.x - position.x, partialPosition.y - position.y, partialPosition.z - position.z);
            //stack.translate(-0.5D, 0.0D, -0.5D);
        }

        MatrixStack[] var12 = matrixStacks;
        var8 = matrixStacks.length;
        //Quaternion conj = currentQuaternion.copy();
        //conj.conj();
        for (int var13 = 0; var13 < var8; ++var13) {
            MatrixStack stack = var12[var13];

            //MatrixTransformStack.of(stack).nudge(this.getId()).centre().rotateY((double)angleYaw).rotateZ((double)anglePitch).rotateY((double)angleInitialYaw).multiply(CurrentAxis,Math.toDegrees(CurrentAxisAngle)).unCentre();
            MatrixTransformStack.of(stack).nudge(this.getId()).centre().rotateY((double) angleYaw).rotateZ((double) anglePitch).rotateY((double) angleInitialYaw).unCentre();
        }

    }

    Profiler profiler;

    public void addTileData(TileEntity te, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        MovementBehaviour movementBehaviour = AllMovementBehaviours.of(block);

        if (te == null)
            return;

        te.getBlockState();

        if (movementBehaviour == null || !movementBehaviour.hasSpecialInstancedRendering()) {
            if (!airshipContraption.maybeInstancedTileEntities.contains(te)) {
                for (int i = 0; i < airshipContraption.maybeInstancedTileEntities.size(); i++) {
                    if (airshipContraption.maybeInstancedTileEntities.get(i).getBlockPos().offset(0, -getPlotPos().getY(), 0).equals(pos)) {
                        airshipContraption.maybeInstancedTileEntities.remove(i);
                        i--;
                    }
                }
                airshipContraption.maybeInstancedTileEntities.add(te);
            }
        }

        airshipContraption.presentTileEntities.put(pos, te);
        if (!airshipContraption.specialRenderedTileEntities.contains(te)) {
            for (int i = 0; i < airshipContraption.specialRenderedTileEntities.size(); i++) {
                if (airshipContraption.specialRenderedTileEntities.get(i).getBlockPos().offset(0, -getPlotPos().getY(), 0).equals(pos)) {
                    airshipContraption.specialRenderedTileEntities.remove(i);
                    i--;
                }
            }
            airshipContraption.specialRenderedTileEntities.add(te);
        }
    }

    public void handle(AirshipContraptionBlockUpdateInfo info) {
        fakeClientWorld.setBlock(
                info.pos,
                info.state,
                1
        );

        if (info.tileEntityNBT != null) {
            TileEntity existingBE = fakeClientWorld.getBlockEntity(info.pos);
            if(existingBE != null) {
                //existingBE.setLevelAndPosition(fakeClientWorld, info.pos);
//                if(existingBE instanceof SmartTileEntity) {
//                    ((SmartTileEntity) existingBE).readClientUpdate(info.state, info.tileEntityNBT);
//                } else {
//                existingBE.getUpdatePacket().handleUpdateTag(info.state, info.tileEntityNBT);
//                }

//                fakeClientWorld.setBlockEntity(info.pos, existingBE);
                addTileData(existingBE, info.pos.offset(0, -getPlotPos().getY(), 0), info.state);
            } else {
                TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(info.tileEntityNBT.getString("id")));
                if (type == null) return;
                TileEntity te = type.create();
                if (te == null) return;

                te.setLevelAndPosition(fakeClientWorld, info.pos);
                te.handleUpdateTag(info.state, info.tileEntityNBT);
                te.load(info.state, info.tileEntityNBT);

                fakeClientWorld.setBlockEntity(info.pos, te);
                addTileData(te, info.pos.offset(0, -getPlotPos().getY(), 0), info.state);
            }
        }
    }

    public void handleControllingSubcontraption(TileEntity be, BlockPos pos) {

        if (!(be instanceof IControlContraption)) return;

        IControlContraption controllingContraption = (IControlContraption) be;

        if (controllingContraption instanceof ContraptionHolderAccessor) {
            ControlledContraptionEntity contraptionEntity = ((ContraptionHolderAccessor) be).getMovedContraption();

            if (contraptionEntity != null) {
                if (!subContraptions.containsKey(contraptionEntity.getUUID())) {
                    stcSubContraptionAddition(contraptionEntity, pos, contraptionEntity.getUUID());
                }

                subContraptions.put(contraptionEntity.getUUID(), contraptionEntity);
                simulatedRigidbody.addSubContraption(contraptionEntity.getUUID(), contraptionEntity);
                ((AbstractContraptionEntityExtension) contraptionEntity).createAeronautics$setOriginalPosition(contraptionEntity.position());
            }
        }
    }

    private void stcSubContraptionAddition(ControlledContraptionEntity contraptionEntity, BlockPos pos, UUID uuid) {
        notifyClients(new AirshipAddSubcontraptionPacket(plotId, contraptionEntity.serializeNBT(), pos, uuid));
    }

    public void addSubcontraptionClient(CompoundNBT nbt, UUID uuid, BlockPos pos) {
        BlockPos plotPos = getPlotPos();

        CompoundNBT controllerTag = nbt.getCompound("Controller");
        controllerTag.put("X", DoubleNBT.valueOf(controllerTag.getDouble("X") - plotPos.getX()));
        controllerTag.put("Z", DoubleNBT.valueOf(controllerTag.getDouble("Z") - plotPos.getZ()));

        Entity entity = EntityType.create(nbt, fakeClientWorld).orElse(null);
        if (entity == null) return;

        ControlledContraptionEntity contraptionEntity = (ControlledContraptionEntity) entity;

        contraptionEntity.move(-plotPos.getX(), 0, -plotPos.getZ());
        ContraptionHandler.addSpawnedContraptionsToCollisionList(contraptionEntity, level);

        fakeClientWorld.addFreshEntity(contraptionEntity);
        subContraptions.put(uuid, contraptionEntity);
        simulatedRigidbody.addSubContraption(uuid, contraptionEntity);
    }

    public void updateSubcontraptionClient(UUID uuid, CompoundNBT nbt) {
        ControlledContraptionEntity contraptionEntity = subContraptions.get(uuid);
        if (contraptionEntity == null) {
            addSubcontraptionClient(nbt, uuid, null);
            contraptionEntity = subContraptions.get(uuid);
        }

        BlockPos plotPos = getPlotPos();
        ListNBT posList = nbt.getList("Pos", Constants.NBT.TAG_DOUBLE);
        posList.set(0, DoubleNBT.valueOf(posList.getDouble(0) - plotPos.getX()));
        posList.set(2, DoubleNBT.valueOf(posList.getDouble(2) - plotPos.getZ()));

        CompoundNBT controllerTag = nbt.getCompound("Controller");
        controllerTag.put("X", DoubleNBT.valueOf(controllerTag.getDouble("X") - plotPos.getX()));
        controllerTag.put("Z", DoubleNBT.valueOf(controllerTag.getDouble("Z") - plotPos.getZ()));

        contraptionEntity.deserializeNBT(nbt);
    }

    public void destroySubcontraptionClient(UUID uuid) {
        ControlledContraptionEntity contraptionEntity = subContraptions.get(uuid);
        if (contraptionEntity == null) return;

        StructureTransform transform = ((ControlledContraptionEntityMixin) contraptionEntity).invokeMakeStructureTransform();

//        contraptionEntity.disassemble();
        contraptionEntity.remove();
        contraptionEntity.getContraption().addBlocksToWorld(fakeClientWorld, transform);
        subContraptions.remove(uuid);
        simulatedRigidbody.removeSubContraption(uuid);
    }

    public Vector3d getPartialPosition(float partialTicks) {
        double x = MathHelper.lerp(partialTicks, xOld, getX());
        double y = MathHelper.lerp(partialTicks, yOld, getY());
        double z = MathHelper.lerp(partialTicks, zOld, getZ());
        Vector3d anchorVec = new Vector3d(x, y, z);

//        Vector3d anchorVec = position().add(physicsManager.globalVelocity.scale(partialTicks).scale(0.05));
        return anchorVec;
    }

    public static class AirshipRotationState extends ContraptionRotationState {
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