package com.eriksonn.createaeronautics.groups;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.index.CABlocks;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModGroup extends ItemGroup {
    public static ModGroup MAIN;;

    public ModGroup(String name) {
        super(CreateAeronautics.MODID+":"+name);
        MAIN = this;
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(CABlocks.TORSION_SPRING.get());
    }
}
