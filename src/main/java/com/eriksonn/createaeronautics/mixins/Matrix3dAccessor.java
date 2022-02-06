package com.eriksonn.createaeronautics.mixins;

import com.eriksonn.createaeronautics.utils.Matrix3dExtension;
import com.simibubi.create.foundation.collision.Matrix3d;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix3d.class)
public abstract class Matrix3dAccessor implements Matrix3dExtension {
    @Shadow private double m00;
    @Shadow private double m01;
    @Shadow private double m02;
    @Shadow private double m10;
    @Shadow private double m11;
    @Shadow private double m12;
    @Shadow private double m20;
    @Shadow private double m21;
    @Shadow private double m22;


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
