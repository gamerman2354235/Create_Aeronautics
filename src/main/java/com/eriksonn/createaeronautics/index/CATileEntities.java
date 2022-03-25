package com.eriksonn.createaeronautics.index;


import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.blocks.airship_assembler.AirshipAssemblerTileEntity;
import com.eriksonn.createaeronautics.blocks.propeller_bearing.PropellerBearingTileEntity;
import com.eriksonn.createaeronautics.blocks.stirling_engine.StirlingEngineInstance;
import com.eriksonn.createaeronautics.blocks.stirling_engine.StirlingEngineRenderer;
import com.eriksonn.createaeronautics.blocks.stirling_engine.StirlingEngineTileEntity;
import com.eriksonn.createaeronautics.blocks.torsion_spring.TorsionSpringTileEntity;
import com.eriksonn.createaeronautics.blocks.stationary_potato_cannon.StationaryPotatoCannonInstance;
import com.eriksonn.createaeronautics.blocks.stationary_potato_cannon.StationaryPotatoCannonRenderer;
import com.eriksonn.createaeronautics.blocks.stationary_potato_cannon.StationaryPotatoCannonTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingRenderer;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftRenderer;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftInstance;
import com.simibubi.create.repack.registrate.util.entry.TileEntityEntry;

public class CATileEntities {


    public static final TileEntityEntry<TorsionSpringTileEntity> TORSION_SPRING = CreateAeronautics.registrate()
            .tileEntity("torsion_spring", TorsionSpringTileEntity::new)
            .instance(() -> SplitShaftInstance::new)
            .validBlocks(CABlocks.TORSION_SPRING)
            .renderer(() -> SplitShaftRenderer::new)
            .register();
    public static final TileEntityEntry<StationaryPotatoCannonTileEntity> STATIONARY_POTATO_CANNON = CreateAeronautics.registrate()
            .tileEntity("stationary_potato_cannon", StationaryPotatoCannonTileEntity::new)
            .instance(() -> StationaryPotatoCannonInstance::new)
            .validBlocks(CABlocks.STATIONARY_POTATO_CANNON)
            .renderer(() -> StationaryPotatoCannonRenderer::new)
            .register();
    public static final TileEntityEntry<AirshipAssemblerTileEntity> AIRSHIP_ASSEMBLER = CreateAeronautics.registrate()
            .tileEntity("airship_assembler", AirshipAssemblerTileEntity::new)
            .validBlocks(CABlocks.AIRSHIP_ASSEMBLER)
            .register();
    public static final TileEntityEntry<PropellerBearingTileEntity> PROPELLER_BEARING = CreateAeronautics.registrate()
            .tileEntity("propeller_bearing", PropellerBearingTileEntity::new)
            .instance(() -> BearingInstance::new)
            .validBlocks(CABlocks.PROPELLER_BEARING)
            .renderer(() -> BearingRenderer::new)
            .register();
    public static final TileEntityEntry<StirlingEngineTileEntity> STIRLING_ENGINE = CreateAeronautics.registrate()
            .tileEntity("stirling_engine", StirlingEngineTileEntity::new)
            .instance(() -> StirlingEngineInstance::new)
            .validBlocks(CABlocks.STIRLING_ENGINE)
            .renderer(() -> StirlingEngineRenderer::new)
            .register();
    public static void register() {}
}
