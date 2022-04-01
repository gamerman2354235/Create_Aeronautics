package com.eriksonn.createaeronautics.physics.collision.shape;

import com.eriksonn.createaeronautics.physics.IRigidbody;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Interface for convex shapes, desgined for use with GJK.
 *
 * @author RyanHCode
 */
public interface ICollisionShape {

    /**
     * Returns the smallest AABB that contains the entire shape.
     *
     * @return The smallest AABB that contains the entire shape.
     */
    AxisAlignedBB getBounds();

    /**
     * Given a direction vector, return the furthest point in that direction.
     *
     * @param direction The direction vector.
     * @return The furthest point in that direction.
     */
    Vector3d support(Vector3d direction);

    /**
     * Returns the center of the shape.
     *
     * @return The center of the shape.
     */
    Vector3d getCenter();

    /**
     * Sets the transform of the shape, to be applied to every vector.
     *
     * @param localPos The translation of the shape.
     * @param transform The rigidbody to use for transformation
     */
    void setTransform(Vector3d localPos, IRigidbody transform);

}
