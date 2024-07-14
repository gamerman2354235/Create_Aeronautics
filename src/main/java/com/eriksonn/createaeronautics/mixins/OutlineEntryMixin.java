package com.eriksonn.createaeronautics.mixins;

import com.simibubi.create.foundation.utility.outliner.Outliner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Outliner.OutlineEntry.class)
public interface OutlineEntryMixin {

    @Accessor
    void setTicksTillRemoval(int ticks);

}
