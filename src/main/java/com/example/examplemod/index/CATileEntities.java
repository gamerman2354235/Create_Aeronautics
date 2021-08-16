package com.example.examplemod.index;


import com.example.examplemod.ExampleMod;
import com.example.examplemod.blocks.airship_assembler.AirshipAssemblerTileEntity;
import com.example.examplemod.blocks.spring_loaded_gearshift.SpringLoadedGearshiftRenderer;
import com.example.examplemod.blocks.spring_loaded_gearshift.SpringLoadedGearshiftTileEntity;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftRenderer;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftInstance;
import com.simibubi.create.content.contraptions.base.HalfShaftInstance;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.repack.registrate.util.entry.TileEntityEntry;

public class CATileEntities {


    public static final TileEntityEntry<SpringLoadedGearshiftTileEntity> SPRING_LOADED_GEARSHIFT = ExampleMod.registrate()
            .tileEntity("spring_loaded_gearshift", SpringLoadedGearshiftTileEntity::new)
            .instance(() -> SplitShaftInstance::new)
            .validBlocks(CABlocks.SPRING_LOADED_GEARSHIFT)
            .renderer(() -> SplitShaftRenderer::new)
            .register();
    public static final TileEntityEntry<AirshipAssemblerTileEntity> AIRSHIP_ASSEMBLER = ExampleMod.registrate()
            .tileEntity("airship_assembler", AirshipAssemblerTileEntity::new)
            .validBlocks(CABlocks.AIRSHIP_ASSEMBLER)
            .register();
    public static void register() {}
}
