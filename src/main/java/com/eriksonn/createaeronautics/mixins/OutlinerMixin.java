package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.render.OutlinerMixinInterface;
import com.eriksonn.createaeronautics.render.PhysicsInfoAABBOutline;
import com.simibubi.create.foundation.utility.outliner.ChasingAABBOutline;
import com.simibubi.create.foundation.utility.outliner.Outline;
import com.simibubi.create.foundation.utility.outliner.Outliner;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Outliner.class)
public abstract class OutlinerMixin implements OutlinerMixinInterface {


    @Shadow @Final private Map<Object, Outliner.OutlineEntry> outlines;

    @Override public PhysicsInfoAABBOutline getAndRefreshPhysicsAABB(Object slot) {
        Outliner.OutlineEntry entry = outlines.get(slot);
        ((OutlineEntryMixin) entry).setTicksTillRemoval(1);
        return (PhysicsInfoAABBOutline) entry.getOutline();
    }

    @Override public Outline.OutlineParams showPhysicsAABB(Object slot, AxisAlignedBB bb) {
        createPhysicsAABBOutlineIfMissing(slot, bb);
        PhysicsInfoAABBOutline outline = getAndRefreshPhysicsAABB(slot);
        return outline.getParams();
    }

    @Override public void createPhysicsAABBOutlineIfMissing(Object slot, AxisAlignedBB bb) {
        if (!outlines.containsKey(slot) || !(outlines.get(slot).getOutline() instanceof PhysicsInfoAABBOutline)) {
            PhysicsInfoAABBOutline outline = new PhysicsInfoAABBOutline(bb);
            outlines.put(slot, new Outliner.OutlineEntry(outline));
        }
    }



}
