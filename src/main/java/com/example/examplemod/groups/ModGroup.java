package com.example.examplemod.groups;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.index.CABlocks;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModGroup extends ItemGroup {
    public static ModGroup MAIN;;

    public ModGroup(String name) {
        super(ExampleMod.MODID+":"+name);
        MAIN = this;
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(CABlocks.SPRING_LOADED_GEARSHIFT.get());
    }
}
