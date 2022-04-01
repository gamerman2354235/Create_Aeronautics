package com.eriksonn.createaeronautics.physics.collision.detection;

import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;
import net.minecraft.util.math.vector.Vector3d;

/**
 * A support point for use with the GJK & EPA algorithms.
 *
 * Contains both info on the point in Minkowski difference space and the individual points on both shapes that formed
 * them, and the support directions used to find them.
 */
public class Support {
    public Vector3d difference;
    public Vector3d pointA;
    public Vector3d pointB;
    public Vector3d direction;

    /**
     * Creates a new support point.
     * @param difference The point in Minkowski difference space.
     * @param pointA The point on shape A that formed the support point.
     * @param pointB The point on shape B that formed the support point.
     * @param direction The direction used to find the point.
     */
    public Support(Vector3d difference, Vector3d pointA, Vector3d pointB, Vector3d direction) {
        this.difference = difference;
        this.pointA = pointA;
        this.pointB = pointB;
        this.direction = direction;
    }

    /**
     * Calculate the Minkowski difference on two shapes given a direction vector.
     *
     * @param a The first shape.
     * @param b The second shape.
     * @param direction The direction vector.
     * @return The Minkowski difference.
     */
    public static Vector3d minkowskiDifference(ICollisionShape a, ICollisionShape b, Vector3d direction) {
        Vector3d oppositeDirection = direction.scale(-1);
        Vector3d support = a.support(direction);
        return support.subtract(b.support(oppositeDirection));
    }

    /**
     * Generates a support point from two shapes and a direction based on the Minkowski difference of the two support points.
     *
     * @param a The first shape.
     * @param b The second shape.
     * @param direction The direction to find the support point in.
     */
    public static Support generate(ICollisionShape a, ICollisionShape b, Vector3d direction) {
        // Get opposite direction for difference
        Vector3d oppositeDirection = direction.scale(-1);

        // Calculate support points
        Vector3d supportA = a.support(direction);
        Vector3d supportB = b.support(oppositeDirection);

        // Calculate difference
        Vector3d minkowskiDifference = supportA.subtract(supportB);

        return new Support(minkowskiDifference, supportA, supportB, direction);
    }
}
