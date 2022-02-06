package com.eriksonn.createaeronautics.utils;

import com.eriksonn.createaeronautics.blocks.airship_assembler.AirshipAssemblerBlock;
import com.simibubi.create.repack.registrate.builders.BlockBuilder;
import com.simibubi.create.repack.registrate.providers.DataGenContext;
import com.simibubi.create.repack.registrate.providers.RegistrateBlockstateProvider;
import com.simibubi.create.repack.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.function.Function;

/**
 * Useful functions for auto-generating models.
 * @author FortressNebula
 * */
public class ModelUtils {
    public static <T extends Block> Function<BlockState, ModelFile> existingModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, String path) {
        return blockState -> prov.models()
                .getExistingFile(prov.modLoc("block/" + path));
    }

    public static <P, T extends Block> NonNullUnaryOperator<BlockBuilder<T, P>> transformExistingItemModel() {
        return b -> b
                .item()
                .model((ctx, prov) -> prov.getExistingFile(prov.modLoc("item/" + ctx.getName())))
                .build();
    }

    public static Function<BlockState, ModelFile> existingModelActive(DataGenContext<?, ?> ctx, RegistrateBlockstateProvider prov, String path) {
        return blockState -> prov.models().getExistingFile(prov.modLoc("block/" + path + (blockState.getValue(AirshipAssemblerBlock.ACTIVE) ? "_on" : "")));
    }
}
