package com.eriksonn.createaeronautics.render;

import com.eriksonn.createaeronautics.render.PhysicsInfoAABBOutline;
import com.simibubi.create.foundation.utility.outliner.Outline;
import net.minecraft.util.math.AxisAlignedBB;

public interface OutlinerMixinInterface {

    PhysicsInfoAABBOutline getAndRefreshPhysicsAABB(Object slot);
    Outline.OutlineParams showPhysicsAABB(Object slot, AxisAlignedBB bb);
    void createPhysicsAABBOutlineIfMissing(Object slot, AxisAlignedBB bb);
}
