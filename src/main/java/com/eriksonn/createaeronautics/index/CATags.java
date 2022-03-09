package com.eriksonn.createaeronautics.index;

import com.eriksonn.createaeronautics.CreateAeronautics;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;


public class CATags {
    public static final ITag.INamedTag<Block> LIGHT = tag("light");
    public static final ITag.INamedTag<Block> HEAVY = tag("heavy");

    public static final ITag.INamedTag<Block> STICKY = tag("sticky");
    public static final ITag.INamedTag<Block> BOUNCY = tag("bouncy");

    public static final ITag.INamedTag<Block> ROUGH = tag("rough");
    public static final ITag.INamedTag<Block> SMOOTH = tag("smooth");

    private static ITag.INamedTag<Block> tag(String path) {
        return BlockTags.bind(CreateAeronautics.asResource(path).toString());
    }
}
