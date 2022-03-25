package com.eriksonn.createaeronautics.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.elements.EntityElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class KineticScenes {
    public static void stirlingEngine(SceneBuilder scene, SceneBuildingUtil util)
    {
        scene.title("stirling_engine", "Generating Rotational Force using the Stirling Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.world.showSection(util.select.layer(0), Direction.UP);
        scene.idle(10);


        scene.world.showSection(util.select.fromTo(2, 1, 1, 2, 2, 3), Direction.DOWN);

        BlockPos enginePos = util.grid.at(2, 2, 1);
        BlockPos shaftPos = util.grid.at(2, 2, 2);
        scene.idle(10);
        scene.overlay.showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(enginePos))
                .text("The Stirling Engine generates rotational force by burning fuel");
        scene.idle(20);
        scene.overlay.showControls(
                new InputWindowElement(util.vector.topOf(enginePos), Pointing.DOWN).withItem(new ItemStack(Items.COAL)),
                30);
        scene.world.cycleBlockProperty(enginePos, AbstractFurnaceBlock.LIT);
        scene.world.setKineticSpeed(util.select.fromTo(2, 2, 1, 2, 2, 3), 32);
        scene.effects.emitParticles(util.vector.of(2.5, 2.2, 0.9), EmitParticlesInstruction.Emitter.simple(ParticleTypes.LAVA, Vector3d.ZERO), 3,
                1);

        scene.idle(80);

        scene.overlay.showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(enginePos.south(2)))
                .text("The engine provides a moderate amount of stress capacity");

        scene.idle(90);

        scene.world.hideSection(util.select.position(shaftPos), Direction.DOWN);
        scene.idle(15);
        BlockState cogState = AllBlocks.COGWHEEL.getDefaultState();
        scene.world.setBlock(shaftPos,cogState.setValue(CogWheelBlock.AXIS, Direction.Axis.Z),false);

        scene.world.showSection(util.select.position(shaftPos).add(util.select.fromTo(0, 1, 1, 1, 2, 2)), Direction.DOWN);
        scene.world.setKineticSpeed(util.select.fromTo(0, 1, 1, 1, 2, 2), -16);

        scene.idle(20);

        scene.overlay.showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.topOf(new BlockPos(0,1,1)))
                .text("Fuel can be inserted by automatic means");

        ItemStack stack = Items.CHARCOAL.getDefaultInstance();
        scene.world.createItemOnBelt(new BlockPos(0,1,1), Direction.WEST, stack);
        scene.idle(10);
        for (int i =0;i<4;i++) {
            scene.idle(24);
            if(i<3) {
                scene.world.createItemOnBelt(new BlockPos(0, 1, 1), Direction.WEST, stack);
            }
            if(i==1)
            {
                scene.markAsFinished();
            }
            scene.idle(10);
            scene.world.removeItemsFromBelt(util.grid.at(1, 1, 1));
            scene.world.flapFunnel(util.grid.at(1, 2, 1), false);
        }
    }
}
