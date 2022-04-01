package com.eriksonn.createaeronautics.physics.collision.detection;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Represents a collision, with a position, normal vector, and contact points on both shapes.
 */
public class Manifold {
    public Vector3d normal;
    public double depth;
    public Vector3d contactPointA;
    public Vector3d contactPointB;

    public Manifold(Vector3d normal, double depth, Vector3d contactPointA, Vector3d contactPointB) {
        this.normal = normal;
        this.depth = depth;
        this.contactPointA = contactPointA;
        this.contactPointB = contactPointB;
    }
}
