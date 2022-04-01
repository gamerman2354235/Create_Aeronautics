package com.eriksonn.createaeronautics.physics;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import net.minecraft.util.math.vector.Vector3d;

public class SubcontraptionRigidbody extends AbstractContraptionRigidbody {
    public SimulatedContraptionRigidbody parentRigidbody;
    public AbstractContraptionEntity entity;
    public SubcontraptionRigidbody(AbstractContraptionEntity entity,SimulatedContraptionRigidbody parentRigidbody)
    {
        this.entity=entity;
        this.parentRigidbody=parentRigidbody;
    }


    public double getMass() {
        return parentRigidbody.getMass();
    }


    public Vector3d getCenterOfMass() {
        return parentRigidbody.getCenterOfMass();
    }


    public Vector3d multiplyInertia(Vector3d v) {
        return parentRigidbody.multiplyInertia(v);
    }


    public Vector3d multiplyInertiaInverse(Vector3d v) {
        return parentRigidbody.multiplyInertiaInverse(v);
    }


    public Vector3d rotate(Vector3d point) {
        return parentRigidbody.rotate(entity.applyRotation(point,0));
    }

    public Vector3d rotateInverse(Vector3d point) {
        return entity.reverseRotation(parentRigidbody.rotateInverse(point),0);
    }
    public Vector3d rotateLocal(Vector3d point) {
        return entity.applyRotation(point,0);
    }

    public Vector3d rotateLocalInverse(Vector3d point) {
        return entity.reverseRotation(point,0);
    }

    public Vector3d toLocal(Vector3d globalPoint) {
        return fromParent(parentRigidbody.toLocal(globalPoint));
    }

    public Vector3d toGlobal(Vector3d localPoint) {

        return parentRigidbody.toGlobal(toParent(localPoint));
    }

    public Vector3d getVelocity() {
        return parentRigidbody.getVelocity();
    }


    public Vector3d getVelocityAtPoint(Vector3d pos) {

        Vector3d parentVelocity = parentRigidbody.getVelocityAtPoint(toParent(pos));
        Vector3d localVelocity = entity.getDeltaMovement();
        localVelocity = localVelocity.add(entity.applyRotation(pos,1).subtract(entity.applyRotation(pos,0)));
        return parentVelocity.add(parentRigidbody.rotate(localVelocity));
    }


    public Vector3d getAngularVelocity() {
        return parentRigidbody.getAngularVelocity();
    }


    public void addForce(Vector3d pos, Vector3d force) {
        parentRigidbody.addForce(toParent(pos),entity.applyRotation(force,0));
    }


    public void addGlobalForce(Vector3d pos, Vector3d force) {
        parentRigidbody.addForce(toParent(pos),force);
    }


    public void applyImpulse(Vector3d pos, Vector3d velocity) {
        parentRigidbody.applyImpulse(toParent(pos),entity.reverseRotation(velocity,0));
    }


    public void applyGlobalImpulse(Vector3d pos, Vector3d velocity) {
        parentRigidbody.applyGlobalImpulse(toParent(pos),velocity);
    }
    Vector3d toParent(Vector3d point)
    {
        Vector3d entityOffsetPosition = entity.position().subtract(parentRigidbody.getPlotOffset());
        return entity.applyRotation(point,0).add(entityOffsetPosition);
    }
    Vector3d fromParent(Vector3d point)
    {
        Vector3d entityOffsetPosition = entity.position().subtract(parentRigidbody.getPlotOffset());
        return entity.reverseRotation(point.subtract(entityOffsetPosition),0);
    }

    @Override
    public AbstractContraptionEntity getContraption() {
        return entity;
    }

    @Override
    public Vector3d getPlotOffset() {
        return parentRigidbody.getPlotOffset();
    }
}
