package com.eriksonn.createaeronautics.physics.collision.detection.impl;

import com.eriksonn.createaeronautics.index.CAConfig;
import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.collision.detection.AbstractContraptionBasedCollisionDetector;
import com.eriksonn.createaeronautics.physics.collision.detection.GJKEPA;
import com.eriksonn.createaeronautics.physics.collision.detection.Manifold;
import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;
import net.minecraft.util.math.BlockPos;

/**
 * Collision detection using the Gilbert-Johnson-Keerthi and expanding polytope algorithms.
 */
public class GJKCollisionDetector extends AbstractContraptionBasedCollisionDetector {

    @Override
    public Manifold test(AbstractContraptionRigidbody rb, ICollisionShape shape, ICollisionShape otherShape, BlockPos localBlockPos, BlockPos worldPos) {
        return GJKEPA.collisionTest(shape, otherShape, CAConfig.MAX_COLLISION_ITERATIONS.get());
    }

}
