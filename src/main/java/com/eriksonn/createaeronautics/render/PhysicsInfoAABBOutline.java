package com.eriksonn.createaeronautics.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;
import net.minecraft.util.math.AxisAlignedBB;

public class PhysicsInfoAABBOutline extends AABBOutline {
    public PhysicsInfoAABBOutline(AxisAlignedBB bb) {
        super(bb);
    }

    @Override
    public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, float pt) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        super.render(ms, buffer, pt);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
