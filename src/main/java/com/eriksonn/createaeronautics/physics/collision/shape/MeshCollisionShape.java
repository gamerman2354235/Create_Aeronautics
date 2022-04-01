package com.eriksonn.createaeronautics.physics.collision.shape;

import com.eriksonn.createaeronautics.physics.IRigidbody;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class MeshCollisionShape implements ICollisionShape {

    public Vector3d[] vertices;

    public MeshCollisionShape(Vector3d[] vertices) {
        this.vertices = vertices;
    }

    @Override
    public AxisAlignedBB getBounds() {
        // Find the min and max values for each axis
        double minX = Float.MAX_VALUE;
        double minY = Float.MAX_VALUE;
        double minZ = Float.MAX_VALUE;
        double maxX = -Float.MAX_VALUE;
        double maxY = -Float.MAX_VALUE;
        double maxZ = -Float.MAX_VALUE;

        for(Vector3d unProcessed : vertices){
            Vector3d v = applyTransform(unProcessed);
            if(v.x < minX) minX = v.x;
            if(v.y < minY) minY = v.y;
            if(v.z < minZ) minZ = v.z;
            if(v.x > maxX) maxX = v.x;
            if(v.y > maxY) maxY = v.y;
            if(v.z > maxZ) maxZ = v.z;
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public Vector3d support(Vector3d direction) {

        // find the vertex with the largest dot product with the direction
        double maxDot = -Float.MAX_VALUE;

        Vector3d maxVertex = null;
        for(Vector3d raw : vertices){
            Vector3d v = applyTransform(raw);
            double dot = v.subtract(getCenter()).dot(direction);
            if(dot > maxDot) {
                maxDot = dot;
                maxVertex =  v;
            }
        }

        return maxVertex;
    }

    @Override
    public Vector3d getCenter() {
        // Average all vertex positions
        Vector3d sum = new Vector3d(0, 0, 0);

        for(Vector3d v : vertices){
            sum = sum.add(applyTransform(v));
        }

        return sum.scale(1.0 / vertices.length);
    }

    // Current translation & rotation transform
    private Vector3d localPos = new Vector3d(0, 0, 0);
    private IRigidbody transform = null;

    public Vector3d applyTransform(Vector3d vec) {
        if(transform == null) return vec.add(this.localPos);
        return transform.toGlobal(vec.add(this.localPos));
    }

    @Override
    public void setTransform(Vector3d localPos, IRigidbody transform) {
        this.localPos = localPos;
        this.transform = transform;
    }
}
