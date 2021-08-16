package com.example.examplemod.index;

import com.example.examplemod.blocks.airship_assembler.AirshipAssemblerBlock;
import com.example.examplemod.groups.ModGroup;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.blocks.spring_loaded_gearshift.SpringLoadedGearshiftBlock;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.relays.encased.SplitShaftInstance;
import com.simibubi.create.foundation.config.StressConfigDefaults;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.repack.registrate.util.entry.BlockEntry;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import net.minecraft.block.AbstractBlock;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class CABlocks {
    private static final CreateRegistrate REGISTRATE = ExampleMod.registrate()
            .itemGroup(() -> ModGroup.MAIN);
    public static final BlockEntry<SpringLoadedGearshiftBlock> SPRING_LOADED_GEARSHIFT = REGISTRATE.block("spring_loaded_gearshift", SpringLoadedGearshiftBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(AbstractBlock.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag) //Dono what this tag means (contraption safe?).
            .item()
            .transform(customItemModel())
            .register();
    public static final BlockEntry<AirshipAssemblerBlock> AIRSHIP_ASSEMBLER = REGISTRATE.block("airship_assembler", AirshipAssemblerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(AbstractBlock.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag) //Dono what this tag means (contraption safe?).
            .item()
            .transform(customItemModel())
            .register();
    public static void register() {
        Create.registrate().addToSection(SPRING_LOADED_GEARSHIFT, AllSections.KINETICS);
    }
}
