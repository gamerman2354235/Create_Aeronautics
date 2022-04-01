package com.eriksonn.createaeronautics.physics.collision.shape;

/**
 * Represents the quality of collision detection.
 * Collision resolution and generation are not affected by this.
 */
public enum CollisionQuality {
    /**
     * Best quality, use of the GJK and EPA algorithms.
     */
    GJKEPA("High (GJK + EPA)"),

    /**
     * Spheres are used to more accurately fit the shape of the block.
     * This can result in possible penetration, however performance is much better.
     */
    SPHERE_AABB("Medium (Sphere AABB)"),

    /**
     * Spheres are used to roughly fit the shape of the block.
     * This can result in possible penetration up to around 1/3 of a block.
     */
    SPHERE("Low (Sphere)");

    private final String name;

    CollisionQuality(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
