package com.example.examplemod.blocks.spring_loaded_gearshift;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;


public class SpringLoadedGearshiftRenderer extends SplitShaftRenderer {

    public SpringLoadedGearshiftRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
        return PartialBufferer.getFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState());
    }
}

