package com.example.examplemod.dimension;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class AirshipWorldData extends WorldSavedData {
    /**
     * ID of this data when it is attached to a world.
     */
    public static final String ID = "airship_storage_dim";



    // Used to allow forward compatibility
    private static final int CURRENT_FORMAT = 2;

    private static final String TAG_FORMAT = "format";

    private static final String TAG_PLOTS = "plots";

    private final Int2ObjectOpenHashMap<AirshipDimensionPlot> plots = new Int2ObjectOpenHashMap<>();
    public AirshipWorldData() {
        super(ID);
    }

    public AirshipDimensionPlot getPlotById(int id) {
        return plots.get(id);
    }

    public List<AirshipDimensionPlot> getPlots() {
        return ImmutableList.copyOf(plots.values());
    }

    public AirshipDimensionPlot allocatePlot(BlockPos size) {

        int nextId = 1;
        for (int id : plots.keySet()) {
            if (id >= nextId) {
                nextId = id + 1;
            }
        }

        AirshipDimensionPlot plot = new AirshipDimensionPlot(nextId,size);
        plots.put(nextId, plot);
        setDirty();
        return plot;
    }

    public void removePlot(int plotId) {
        plots.remove(plotId);
        setDirty();
    }
    @Override
    public void load(CompoundNBT tag) {
        int version = tag.getInt(TAG_FORMAT);
        if (version != CURRENT_FORMAT) {
            // Currently no new format has been defined, as such anything but the current
            // version is invalid
            throw new IllegalStateException("Invalid AE2 spatial info version: " + version);
        }

        ListNBT plotsTag = tag.getList(TAG_PLOTS, Constants.NBT.TAG_COMPOUND);
        for (INBT plotTag : plotsTag) {
            AirshipDimensionPlot plot = AirshipDimensionPlot.fromTag((CompoundNBT) plotTag);

            if (plots.containsKey(plot.getId())) {
                //AELog.warn("Overwriting duplicate plot id %s", plot.getId());
            }
            plots.put(plot.getId(), plot);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag.putInt(TAG_FORMAT, CURRENT_FORMAT);

        ListNBT plotTags = new ListNBT();
        for (AirshipDimensionPlot plot : plots.values()) {
            plotTags.add(plot.toTag());
        }
        tag.put(TAG_PLOTS, plotTags);

        return tag;
    }
}
