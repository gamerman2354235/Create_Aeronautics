package com.example.examplemod.mixins;

import com.example.examplemod.dimension.AirshipDimensionManager;
import com.example.examplemod.dimension.ChunkGenerator;
import com.mojang.serialization.Lifecycle;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

@Mixin(value = DimensionType.class)
public class DimensionTypeMixin {
    public DimensionTypeMixin() {
    }

    @Invoker("<init>")
    static DimensionType create(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultrawarm, boolean natural, double coordinateScale, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int logicalHeight, ResourceLocation infiniburn, ResourceLocation skyProperties, float ambientLight) {
        throw new AssertionError();
    }

    @Inject(
            method = {"registerBuiltin"},
            at = {@At("TAIL")}
    )
    private static void addRegistryDefaults(DynamicRegistries.Impl registryTracker, CallbackInfoReturnable<?> cir) {
        DimensionType dimensionType = create(OptionalLong.of(12000L), false, false, false, false, 1.0D, false, false, false, false, 256, BlockTags.INFINIBURN_OVERWORLD.getName(), AirshipDimensionManager.SKY_PROPERTIES_ID, 1.0F);
        Registry.register(registryTracker.dimensionTypes(), AirshipDimensionManager.DIMENSION_TYPE_ID.location(), dimensionType);
    }
    /**
     * Insert our custom dimension into the initial registry. <em>This is what will ultimately lead to the creation of a
     * new World.</em>
     */
    @Inject(method = "defaultDimensions", at = @At("RETURN"))
    private static void buildDimensionRegistry(Registry<DimensionType> dimensionTypes, Registry<Biome> biomes,
                                               Registry<DimensionSettings> dimensionSettings, long seed,
                                               CallbackInfoReturnable<SimpleRegistry<Dimension>> cir) {
        SimpleRegistry<Dimension> simpleregistry = cir.getReturnValue();

        simpleregistry.register(AirshipDimensionManager.DIMENSION_ID,
                new Dimension(() -> dimensionTypes.getOrThrow(AirshipDimensionManager.DIMENSION_TYPE_ID),
                        new ChunkGenerator(biomes)),
                Lifecycle.stable());

    }
}
