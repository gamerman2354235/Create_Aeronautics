package com.eriksonn.createaeronautics.physics.collision.detection.impl;

import com.eriksonn.createaeronautics.index.CAConfig;
import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.collision.detection.AbstractContraptionBasedCollisionDetector;
import com.eriksonn.createaeronautics.physics.collision.detection.Manifold;
import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Collision detection using sphere-sphere collision detection.
 */
public class SphereCollisionDetector extends AbstractContraptionBasedCollisionDetector {


    @Override
    public Manifold test(AbstractContraptionRigidbody rb, ICollisionShape shape, ICollisionShape otherShape, BlockPos localBlockPos, BlockPos worldPos) {
        Vector3d spherePositionA = rb.toGlobal(new Vector3d(localBlockPos.getX(), localBlockPos.getY(), localBlockPos.getZ()).add(0.5,0.5,0.5)/*.add(rb.getCenterOfMass())*/);
        Vector3d spherePositionB = new Vector3d(worldPos.getX(), worldPos.getY(), worldPos.getZ()).add(0.5,0.5,0.5);

        if(spherePositionA.distanceToSqr(spherePositionB) < 0.001) return null;

        double radiusA = 0.5;
        double radiusB = 0.5;

        // Test for collision
        double distance = spherePositionA.distanceToSqr(spherePositionB);
        double radiusSum = radiusA + radiusB;
        if (distance < radiusSum * radiusSum) {
            // Collision detected
            Vector3d normal = spherePositionA.subtract(spherePositionB).normalize();
            double penetration = radiusSum - Math.sqrt(distance);

            if (penetration < 0) {
                return null;
            }

            // Position of the collision point
            Vector3d collisionPointA = spherePositionA.add(normal.scale(-radiusA));
            Vector3d collisionPointB = spherePositionB.add(normal.scale(radiusB));

            return new Manifold(normal.scale(-1.0), penetration, collisionPointA, collisionPointB);
        } else {
            return null;
        }
    }


    /*

            Vector3d spherePositionA = rb.toGlobal(new Vector3d(localBlockPos.getX(), localBlockPos.getY(), localBlockPos.getZ()).add(0.5,0.5,0.5));
        Vector3d spherePositionB = new Vector3d(worldPos.getX(), worldPos.getY(), worldPos.getZ()).add(0.5,0.5,0.5);

        if(spherePositionA.distanceToSqr(spherePositionB) < 0.001) return null;

        double radiusA = 0.5;
        double radiusB = 0.5;

        // Test for collision
        double distance = spherePositionA.distanceToSqr(spherePositionB);
        double radiusSum = radiusA + radiusB;
        if (distance < radiusSum * radiusSum) {
            // Collision detected
            Vector3d normal = spherePositionA.subtract(spherePositionB).normalize();
            double penetration = radiusSum - Math.sqrt(distance);

            if (penetration < 0) {
                return null;
            }

            // Position of the collision point
            Vector3d collisionPointA = spherePositionA.add(normal.scale(-radiusA));
            Vector3d collisionPointB = spherePositionB.add(normal.scale(radiusB));

            return new Manifold(normal.scale(-1.0), penetration, collisionPointA, collisionPointB);
        } else {
            return null;
        }
    }





     */

}


