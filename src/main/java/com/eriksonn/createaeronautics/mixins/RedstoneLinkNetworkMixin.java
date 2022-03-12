package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.IRedstoneLinkable;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Set;

@Mixin(RedstoneLinkNetworkHandler.class)
public class RedstoneLinkNetworkMixin {

    @Inject(method = "updateNetworkOf", remap = false, at = @At("HEAD"))
    public void updateNetworkOf(IWorld world, IRedstoneLinkable inRealWorldActor, CallbackInfo ci) {

        // airship dimension trickery
        ServerWorld airshipDimension = AirshipDimensionManager.INSTANCE.getWorld();
        if(world != airshipDimension) {

            // iterate over all airships in the world
            for(AirshipContraptionEntity airship : AirshipManager.INSTANCE.AllAirships.values()) {

                // plotpos
                BlockPos plotPos = airship.getPlotPos();

                // get network
                Set<IRedstoneLinkable> airshipNetwork = Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(airshipDimension, inRealWorldActor);
                Set<IRedstoneLinkable> realWorldNetwork = Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(world, inRealWorldActor);

                int power = 0;

                for (Iterator<IRedstoneLinkable> iterator = airshipNetwork.iterator(); iterator.hasNext();) {
                    IRedstoneLinkable inAirshipDimensionActor = iterator.next();
                    if (!inAirshipDimensionActor.isAlive()) {
                        iterator.remove();
                        continue;
                    }

                    if(!withinRange(inRealWorldActor, airship, plotPos, inAirshipDimensionActor)) {
                        iterator.remove();
                        continue;
                    }

                    if (power < 15)
                        power = Math.max(inAirshipDimensionActor.getTransmittedStrength(), power);

                    for (Iterator<IRedstoneLinkable> iterator1 = realWorldNetwork.iterator(); iterator1.hasNext();) {
                        IRedstoneLinkable inRealWorldActor2 = iterator1.next();

                        if(!inRealWorldActor2.isAlive()) {
                            iterator1.remove();
                            continue;
                        }

                        if(!withinRange(inRealWorldActor2, airship, plotPos, inAirshipDimensionActor)) {
                            iterator1.remove();
                            continue;
                        }

                        if (power < 15)
                            power = Math.max(inRealWorldActor2.getTransmittedStrength(), power);
                    }
                }

                if (inRealWorldActor instanceof LinkBehaviour) {
                    LinkBehaviour linkBehaviour = (LinkBehaviour) inRealWorldActor;
                    // fix one-to-one loading order problem
                    if (linkBehaviour.isListening()) {
                        linkBehaviour.newPosition = true;
                        linkBehaviour.setReceivedStrength(power);
                    }
                }

                for (IRedstoneLinkable other : airshipNetwork) {
                    if (other != inRealWorldActor && other.isListening() && withinRange(inRealWorldActor, airship, plotPos, other))
                        other.setReceivedStrength(power);
                }

            }

        }

    }

    private boolean withinRange(IRedstoneLinkable actor, AirshipContraptionEntity airship, BlockPos plotPos, IRedstoneLinkable other) {
        // get position of other relative to airship
        BlockPos otherPos = other.getLocation().subtract(plotPos);

        // get position in overworld
        Vector3d overworldPos = airship.toGlobalVector(new Vector3d(otherPos.getX(), otherPos.getY(), otherPos.getZ()), 1.0f);

        // range check
        return actor.getLocation().closerThan(new BlockPos(overworldPos), AllConfigs.SERVER.logistics.linkRange.get());
    }


}
