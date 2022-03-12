package com.eriksonn.createaeronautics.physics;

import net.minecraft.util.math.vector.Vector3d;

public interface IRigidbody {

    //#region Structure
    double getMass();
    double getLocalMass();
    Vector3d getCenterOfMass();
    Vector3d getLocalCenterOfMass();
    Vector3d multiplyInertia(Vector3d v);
    Vector3d multiplyInertiaInverse(Vector3d v);
    //#endregion

    //#region Movement
    Vector3d rotate(Vector3d point);
    Vector3d rotateInverse(Vector3d point);
    Vector3d toLocal(Vector3d globalPoint);
    Vector3d toGlobal(Vector3d localPoint);
    Vector3d getVelocity();
    Vector3d getVelocityAtPoint(Vector3d pos);
    Vector3d getAngularVelocity();
    //#endregion

    //#region Interaction
    void addForce(Vector3d pos,Vector3d force);
    void addGlobalForce(Vector3d pos,Vector3d force);
    void addVelocity(Vector3d pos,Vector3d velocity);
    void addGlobalVelocity(Vector3d pos,Vector3d velocity);
    //#endregion
}
