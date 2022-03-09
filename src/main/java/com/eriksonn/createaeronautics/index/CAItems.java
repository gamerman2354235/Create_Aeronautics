package com.eriksonn.createaeronautics.index;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.groups.CAItemGroups;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.repack.registrate.util.entry.ItemEntry;
import net.minecraft.item.Item;

public class CAItems {
    private static final CreateRegistrate REGISTRATE = CreateAeronautics.registrate()
            .itemGroup(() -> CAItemGroups.MAIN_GROUP);

    public static ItemEntry<Item> ENDSTONE_POWDER = REGISTRATE
            .item("endstone_powder", Item::new)
            .register();

    public static void register(){}
}
