package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.utils.Matrix3dExtension;
import com.simibubi.create.foundation.collision.Matrix3d;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix3d.class)
public abstract class Matrix3dAccessor implements Matrix3dExtension {
    @Shadow(remap = false) double m00;
    @Shadow(remap = false) double m01;
    @Shadow(remap = false) double m02;
    @Shadow(remap = false) double m10;
    @Shadow(remap = false) double m11;
    @Shadow(remap = false) double m12;
    @Shadow(remap = false) double m20;
    @Shadow(remap = false) double m21;
    @Shadow(remap = false) double m22;


    public void createaeronautics$set(Vector3d I, Vector3d J, Vector3d K) {
        m00 = I.x;
        m01 = I.y;
        m02 = I.z;

        m10 = J.x;
        m11 = J.y;
        m12 = J.z;

        m20 = K.x;
        m21 = K.y;
        m22 = K.z;
    }
}
