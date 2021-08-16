package com.example.examplemod.mixins;

import com.example.examplemod.blocks.spring_loaded_gearshift.SpringLoadedGearshiftTileEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.relays.encased.DirectionalShaftHalvesTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearboxTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Iterate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(RotationPropagator.class)
public abstract class RotationPropagatorMixin {

    private static float getRotationSpeedModifier(KineticTileEntity from, KineticTileEntity to) {
        BlockState stateFrom = from.getBlockState();
        BlockState stateTo = to.getBlockState();
        Block fromBlock = stateFrom.getBlock();
        Block toBlock = stateTo.getBlock();
        if (fromBlock instanceof IRotate && toBlock instanceof IRotate) {
            IRotate definitionFrom = (IRotate)fromBlock;
            IRotate definitionTo = (IRotate)toBlock;
            BlockPos diff = to.getBlockPos().subtract(from.getBlockPos());
            Direction direction = Direction.getNearest((float)diff.getX(), (float)diff.getY(), (float)diff.getZ());
            World world = from.getLevel();
            boolean alignedAxes = true;
            Axis[] var12 = Axis.values();
            int var13 = var12.length;

            Axis sourceAxis;
            for(int var14 = 0; var14 < var13; ++var14) {
                sourceAxis = var12[var14];
                if (sourceAxis != direction.getAxis() && sourceAxis.choose(diff.getX(), diff.getY(), diff.getZ()) != 0) {
                    alignedAxes = false;
                }
            }

            boolean connectedByAxis = alignedAxes && definitionFrom.hasShaftTowards(world, from.getBlockPos(), stateFrom, direction) && definitionTo.hasShaftTowards(world, to.getBlockPos(), stateTo, direction.getOpposite());
            boolean connectedByGears = ICogWheel.isSmallCog(stateFrom) && ICogWheel.isSmallCog(stateTo);
            float custom = from.propagateRotationTo(to, stateFrom, stateTo, diff, connectedByAxis, connectedByGears);
            if (custom != 0.0F) {

                return custom;
            } else if (connectedByAxis) {
                float axisModifier = getAxisModifier(to, direction.getOpposite());
                if (axisModifier != 0.0F) {
                    axisModifier = 1.0F / axisModifier;
                }

                return getAxisModifier(from, direction) * axisModifier;
            } else if (fromBlock instanceof EncasedBeltBlock && toBlock instanceof EncasedBeltBlock) {
                boolean connected = EncasedBeltBlock.areBlocksConnected(stateFrom, stateTo, direction);
                return connected ? EncasedBeltBlock.getRotationSpeedModifier(from, to) : 0.0F;
            } else if (isLargeToLargeGear(stateFrom, stateTo, diff)) {
                sourceAxis = (Axis)stateFrom.getValue(BlockStateProperties.AXIS);
                Axis targetAxis = (Axis)stateTo.getValue(BlockStateProperties.AXIS);
                int sourceAxisDiff = sourceAxis.choose(diff.getX(), diff.getY(), diff.getZ());
                int targetAxisDiff = targetAxis.choose(diff.getX(), diff.getY(), diff.getZ());
                return sourceAxisDiff > 0 ^ targetAxisDiff > 0 ? -1.0F : 1.0F;
            } else if (ICogWheel.isLargeCog(stateFrom) && ICogWheel.isSmallCog(stateTo) && isLargeToSmallCog(stateFrom, stateTo, definitionTo, diff)) {
                return -2.0F;
            } else if (ICogWheel.isLargeCog(stateTo) && ICogWheel.isSmallCog(stateFrom) && isLargeToSmallCog(stateTo, stateFrom, definitionFrom, diff)) {
                return -0.5F;
            } else {
                if (connectedByGears) {
                    if (diff.distManhattan(BlockPos.ZERO) != 1) {
                        return 0.0F;
                    }

                    if (ICogWheel.isLargeCog(stateTo)) {
                        return 0.0F;
                    }

                    if (direction.getAxis() == definitionFrom.getRotationAxis(stateFrom)) {
                        return 0.0F;
                    }

                    if (definitionFrom.getRotationAxis(stateFrom) == definitionTo.getRotationAxis(stateTo)) {
                        return -1.0F;
                    }
                }

                return 0.0F;
            }
        } else {
            return 0.0F;
        }
    }

    private static float getConveyedSpeed(KineticTileEntity from, KineticTileEntity to) {
        BlockState stateFrom = from.getBlockState();
        BlockState stateTo = to.getBlockState();
        if (isLargeCogToSpeedController(stateFrom, stateTo, to.getBlockPos().subtract(from.getBlockPos()))) {
            return SpeedControllerTileEntity.getConveyedSpeed(from, to, true);
        } else if (isLargeCogToSpeedController(stateTo, stateFrom, from.getBlockPos().subtract(to.getBlockPos()))) {
            return SpeedControllerTileEntity.getConveyedSpeed(to, from, false);
        } else {
            float rotationSpeedModifier = getRotationSpeedModifier(from, to);
            return from.getTheoreticalSpeed() * rotationSpeedModifier;
        }
    }
    private static boolean isLargeToLargeGear(BlockState from, BlockState to, BlockPos diff) {
        if (ICogWheel.isLargeCog(from) && ICogWheel.isLargeCog(to)) {
            Axis fromAxis = (Axis)from.getValue(BlockStateProperties.AXIS);
            Axis toAxis = (Axis)to.getValue(BlockStateProperties.AXIS);
            if (fromAxis == toAxis) {
                return false;
            } else {
                Axis[] var5 = Axis.values();
                int var6 = var5.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    Axis axis = var5[var7];
                    int axisDiff = axis.choose(diff.getX(), diff.getY(), diff.getZ());
                    if (axis != fromAxis && axis != toAxis) {
                        if (axisDiff != 0) {
                            return false;
                        }
                    } else if (axisDiff == 0) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }
    private static float getAxisModifier(KineticTileEntity te, Direction direction) {
        if ((te.hasSource()||te.isSource()) && te instanceof DirectionalShaftHalvesTileEntity) {

            if (te instanceof GearboxTileEntity) {
                Direction source = ((DirectionalShaftHalvesTileEntity)te).getSourceFacing();
                return direction.getAxis() == source.getAxis() ? (direction == source ? 1.0F : -1.0F) : (direction.getAxisDirection() == source.getAxisDirection() ? -1.0F : 1.0F);
            } else {
                return te instanceof SplitShaftTileEntity ? ((SplitShaftTileEntity)te).getRotationSpeedModifier(direction) : 1.0F;
            }
        } else {
            return 1.0F;
        }
    }

    private static boolean isLargeToSmallCog(BlockState from, BlockState to, IRotate defTo, BlockPos diff) {
        Axis axisFrom = (Axis)from.getValue(BlockStateProperties.AXIS);
        if (axisFrom != defTo.getRotationAxis(to)) {
            return false;
        } else if (axisFrom.choose(diff.getX(), diff.getY(), diff.getZ()) != 0) {
            return false;
        } else {
            Axis[] var5 = Axis.values();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Axis axis = var5[var7];
                if (axis != axisFrom && Math.abs(axis.choose(diff.getX(), diff.getY(), diff.getZ())) != 1) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean isLargeCogToSpeedController(BlockState from, BlockState to, BlockPos diff) {
        if (ICogWheel.isLargeCog(from) && AllBlocks.ROTATION_SPEED_CONTROLLER.has(to)) {
            if (!diff.equals(BlockPos.ZERO.below())) {
                return false;
            } else {
                Axis axis = (Axis)from.getValue(CogWheelBlock.AXIS);
                if (axis.isVertical()) {
                    return false;
                } else {
                    return to.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) != axis;
                }
            }
        } else {
            return false;
        }
    }
    /**
     * @author Eriksonn
     */
    @Overwrite
    private static void propagateNewSource(KineticTileEntity currentTE) {

        BlockPos pos = currentTE.getBlockPos();
        World world = currentTE.getLevel();

        Iterator var3 = getConnectedNeighbours(currentTE).iterator();
        //Iterator var0 = getConnectedNeighbours(currentTE).iterator();
        //System.out.println("currentpropagator:" + currentTE.toString());
        //while(var0.hasNext()) {
        //    System.out.println("neighbour:" + var0.next().toString());
        //}

        while(true) {
            KineticTileEntity neighbourTE;
            float speedOfCurrent;
            float speedOfNeighbour;
            float newSpeed;
            float oppositeSpeed;
            do {
                if (!var3.hasNext()) {
                    return;
                }

                neighbourTE = (KineticTileEntity)var3.next();
                speedOfCurrent = currentTE.getTheoreticalSpeed();
                speedOfNeighbour = neighbourTE.getTheoreticalSpeed();
                newSpeed = getConveyedSpeed(currentTE, neighbourTE);
                oppositeSpeed = getConveyedSpeed(neighbourTE, currentTE);
            } while(newSpeed == 0.0F && oppositeSpeed == 0.0F);

            boolean incompatible = Math.signum(newSpeed) != Math.signum(speedOfNeighbour) && newSpeed != 0.0F && speedOfNeighbour != 0.0F;
            boolean tooFast = Math.abs(newSpeed) > (float)(Integer)AllConfigs.SERVER.kinetics.maxRotationSpeed.get();
            boolean speedChangedTooOften = currentTE.getFlickerScore() > 128;
            if (tooFast || speedChangedTooOften) {
                world.destroyBlock(pos, true);
                return;
            }

            if (incompatible) {
                world.destroyBlock(pos, true);
                return;
            }

            float prevSpeed;
            if (Math.abs(oppositeSpeed) > Math.abs(speedOfCurrent)) {
                prevSpeed = currentTE.getSpeed();
                currentTE.setSource(neighbourTE.getBlockPos());
                currentTE.setSpeed(getConveyedSpeed(neighbourTE, currentTE));
                currentTE.onSpeedChanged(prevSpeed);
                currentTE.sendData();
                propagateNewSource(currentTE);
                return;
            }

            if (Math.abs(newSpeed) >= Math.abs(speedOfNeighbour)) {
                if (currentTE.hasNetwork() && !currentTE.network.equals(neighbourTE.network)) {
                    if (currentTE.hasSource() && currentTE.source.equals(neighbourTE.getBlockPos())) {
                        currentTE.removeSource();
                    }

                    prevSpeed = neighbourTE.getSpeed();
                    neighbourTE.setSource(currentTE.getBlockPos());
                    neighbourTE.setSpeed(getConveyedSpeed(currentTE, neighbourTE));
                    neighbourTE.onSpeedChanged(prevSpeed);
                    neighbourTE.sendData();
                    propagateNewSource(neighbourTE);
                } else {
                    prevSpeed = Math.abs(speedOfNeighbour) / 256.0F / 256.0F;
                    if (Math.abs(newSpeed) > Math.abs(speedOfNeighbour) + prevSpeed) {
                        world.destroyBlock(pos, true);
                    }
                }
            } else if (neighbourTE.getTheoreticalSpeed() != newSpeed) {
                prevSpeed = neighbourTE.getSpeed();
                neighbourTE.setSpeed(newSpeed);
                neighbourTE.setSource(currentTE.getBlockPos());
                neighbourTE.onSpeedChanged(prevSpeed);
                neighbourTE.sendData();
                propagateNewSource(neighbourTE);
            }
        }
    }
    private static KineticTileEntity findConnectedNeighbour(KineticTileEntity currentTE, BlockPos neighbourPos) {
        BlockState neighbourState = currentTE.getLevel().getBlockState(neighbourPos);
        if (!(neighbourState.getBlock() instanceof IRotate)) {
            return null;
        } else if (!neighbourState.hasTileEntity()) {
            return null;
        } else {
            TileEntity neighbourTE = currentTE.getLevel().getBlockEntity(neighbourPos);
            if (!(neighbourTE instanceof KineticTileEntity)) {
                return null;
            } else {
                KineticTileEntity neighbourKTE = (KineticTileEntity)neighbourTE;
                if (!(neighbourKTE.getBlockState().getBlock() instanceof IRotate)) {
                    return null;
                } else {
                    boolean a = isConnected2(currentTE, neighbourKTE);
                    boolean b = isConnected2(neighbourKTE, currentTE);
                    if(currentTE instanceof SpringLoadedGearshiftTileEntity||neighbourTE instanceof SpringLoadedGearshiftTileEntity) {
                        //System.out.println("current:" + currentTE.toString());
                        //System.out.println("neighbour:" + neighbourTE.toString());
                        //System.out.println("a:" + a + " b:" + b);
                        //System.out.println("out:"+(!a && !b));
                        //boolean a2 = isConnected2(currentTE, neighbourKTE);
                        //boolean b2 = isConnected2(neighbourKTE, currentTE);
                    }
                    boolean B = !a && !b;

                    return B ? null : neighbourKTE;
                }
            }
        }
    }

    private static boolean isConnected2(KineticTileEntity from, KineticTileEntity to) {
        BlockState stateFrom = from.getBlockState();
        BlockState stateTo = to.getBlockState();
        return isLargeCogToSpeedController(stateFrom, stateTo, to.getBlockPos().subtract(from.getBlockPos())) || getRotationSpeedModifier(from, to) != 0.0F || from.isCustomConnection(to, stateFrom, stateTo);
    }
    private static List<KineticTileEntity> getConnectedNeighbours(KineticTileEntity te) {
        List<KineticTileEntity> neighbours = new LinkedList();
        Iterator var2 = getPotentialNeighbourLocations(te).iterator();



        while(var2.hasNext()) {
            BlockPos neighbourPos = (BlockPos)var2.next();
            KineticTileEntity neighbourTE = findConnectedNeighbour(te, neighbourPos);
            if (neighbourTE != null) {
                neighbours.add(neighbourTE);
            }
        }

        return neighbours;
    }
    private static List<BlockPos> getPotentialNeighbourLocations(KineticTileEntity te) {
        List<BlockPos> neighbours = new LinkedList();


        if (!te.getLevel().isAreaLoaded(te.getBlockPos(), 1)) {
            return neighbours;
        } else {
            Direction[] var2 = Iterate.directions;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Direction facing = var2[var4];
                neighbours.add(te.getBlockPos().relative(facing));
            }

            BlockState blockState = te.getBlockState();
            if (!(blockState.getBlock() instanceof IRotate)) {
                return neighbours;
            } else {
                IRotate block = (IRotate)blockState.getBlock();
                return te.addPropagationLocations(block, blockState, neighbours);
            }
        }
    }
}

