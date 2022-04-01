package com.eriksonn.createaeronautics.physics.collision.detection;

import net.minecraft.util.math.vector.Vector3d;

public class Triangle {
    public Vector3d[] vertices;
    public Vector3d normal;

    public Triangle(Vector3d[] vertices) {
        this.vertices = vertices;
        this.normal = getNormal();
    }

    public Triangle(Vector3d v1, Vector3d v2, Vector3d v3) {
        this.vertices = new Vector3d[] {v1, v2, v3};
        this.normal = getNormal();
    }

    public Vector3d getNormal() {
        Vector3d u = vertices[1].subtract(vertices[0]);
        Vector3d v = vertices[2].subtract(vertices[0]);

        return u.cross(v).normalize();
    }
}
