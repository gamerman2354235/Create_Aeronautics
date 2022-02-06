package com.eriksonn.createaeronautics.utils;

import com.simibubi.create.foundation.collision.Matrix3d;
import net.minecraft.util.math.vector.Vector3d;

public interface Matrix3dExtension {
    void createaeronautics$set(Vector3d I, Vector3d J, Vector3d K);

    static void set(Matrix3d m, Vector3d I, Vector3d J, Vector3d K) {
        ((Matrix3dExtension) m).createaeronautics$set(I, J, K);
    }
}
