package com.eriksonn.createaeronautics.physics.collision.detection.impl;

import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.collision.detection.AbstractContraptionBasedCollisionDetector;
import com.eriksonn.createaeronautics.physics.collision.detection.Manifold;
import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Collision detection using sphere-sphere collision detection.
 */
public class SphereAABBCollisionDetector extends AbstractContraptionBasedCollisionDetector {

    public static double getVectorValueAtIndex(Vector3d vector, int index) {
        switch (index) {
            case 0:
                return vector.x;
            case 1:
                return vector.y;
            case 2:
                return vector.z;
            default:
                throw new IllegalArgumentException("Index must be between 0 and 2");
        }
    }

    public static Vector3d setVectorValueAtIndex(Vector3d vector, int index, double value) {
        switch (index) {
            case 0:
                vector = new Vector3d(value, vector.y, vector.z);
                break;
            case 1:
                vector = new Vector3d(vector.x, value, vector.z);
                break;
            case 2:
                vector = new Vector3d(vector.x, vector.y, value);
                break;
            default:
                throw new IllegalArgumentException("Index must be between 0 and 2");
        }

        return vector;
    }

    public static Vector3d closestPointAABB(Vector3d point, AxisAlignedBB aabb) {
        Vector3d q = new Vector3d(0.0, 0.0, 0.0);

        Vector3d min = new Vector3d(aabb.minX, aabb.minY, aabb.minZ);
        Vector3d max = new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ);

        for( int i = 0; i < 3; i++ ) {
            double v = getVectorValueAtIndex(point, i);
            if( v < getVectorValueAtIndex(min, i) ) v = getVectorValueAtIndex(min, i);
            if( v > getVectorValueAtIndex(max, i) ) v = getVectorValueAtIndex(max, i);
            q = setVectorValueAtIndex(q, i, v);
        }
        return q;
    }

    @Override
    public Manifold test(AbstractContraptionRigidbody rb, ICollisionShape shape, ICollisionShape otherShape, BlockPos localBlockPos, BlockPos worldPos) {
        Vector3d spherePositionA = rb.toGlobal(new Vector3d(localBlockPos.getX(), localBlockPos.getY(), localBlockPos.getZ()).add(0.5,0.5,0.5)/*.add(rb.getCenterOfMass())*/);
        AxisAlignedBB aabb = new AxisAlignedBB(worldPos);

        if(spherePositionA.distanceToSqr(aabb.getCenter()) < 0.001) return null;

        double radiusA = 0.5;

        // Test for collision
        Vector3d closestPointA = closestPointAABB(spherePositionA, aabb);
        double distance = spherePositionA.distanceToSqr(closestPointA);
        double radiusSum = radiusA;
        if (distance < radiusSum * radiusSum) {
            // Collision detected
            Vector3d normal = spherePositionA.subtract(closestPointA).normalize();
            double penetration = radiusSum - Math.sqrt(distance);

            if (penetration < 0) {
                return null;
            }

            // Position of the collision point
            Vector3d collisionPointA = spherePositionA.add(normal.scale(-radiusA));
            Vector3d collisionPointB = closestPointA;

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


