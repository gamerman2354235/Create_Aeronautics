package com.eriksonn.createaeronautics.groups;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.index.CABlocks;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CAItemGroups {
    public static ItemGroup MAIN_GROUP = new ItemGroup("main_group") {
        @Override
        public ItemStack makeIcon() {
            return CABlocks.AIRSHIP_ASSEMBLER.asStack();
        }
    };

    // Tell Registrate to create a lang entry for the item groups
    private static final CreateRegistrate REGISTRATE = CreateAeronautics.registrate().itemGroup(() -> MAIN_GROUP, "Create: Aeronautics");
}
