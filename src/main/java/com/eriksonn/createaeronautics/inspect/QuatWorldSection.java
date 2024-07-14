package com.eriksonn.createaeronautics.inspect;

import com.eriksonn.createaeronautics.physics.SimulatedContraptionRigidbody;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

public class QuatWorldSection extends WorldSectionElement {

    SimulatedContraptionRigidbody rigidbody;

    public QuatWorldSection(SimulatedContraptionRigidbody rigidbody) {
        this.rigidbody = rigidbody;
    }

    public QuatWorldSection(SimulatedContraptionRigidbody rigidbody, Selection selection) {
        super(selection);
        this.rigidbody = rigidbody;
    }

    private Vector3d reverseTransformVec(Vector3d in) {
        return new Vector3d(in.x, in.y, -in.z);
    }

    Vector3d prevAnimatedOffset = new Vector3d(0, 0, 0);

    @Override
    public void tick(PonderScene scene) {
        prevAnimatedOffset = getAnimatedOffset();
        super.tick(scene);
    }

    Vector3d center;

    @Override
    public void setCenterOfRotation(Vector3d center) {
        this.center = center;
        super.setCenterOfRotation(center);
    }


    @Override
    public void transformMS(MatrixStack ms, float pt) {
        MatrixTransformStack.of(ms)
                .translate(VecHelper.lerp(pt, prevAnimatedOffset, getAnimatedOffset()));
        ms.translate(center.x, center.y, center.z);

        Quaternion quat = rigidbody.getPartialOrientation(pt);
        // invert the quaternion
        quat.conj();

        ms.mulPose(quat);
        ms.translate(-center.x, -center.y, -center.z);
    }
}