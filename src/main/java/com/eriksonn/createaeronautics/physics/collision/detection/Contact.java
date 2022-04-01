package com.eriksonn.createaeronautics.physics.collision.detection;

import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Represents a contact between two contraptions, containing both a manifold and information about the colliding blocks.
 */
public class Contact {
    public Manifold manifold;
    public BlockState blockA, blockB;
    public AbstractContraptionRigidbody rigidbody;

    /**
     * Impulse accumulated in normal direction
     */
    public double normalImpulse = 0;

    /**
     * Impulse accumulated in the tangent direction
     */
    public double tangentImpulse = 0;

    /**
     * Effective mass in the normal direction
     */
    public double normalMass = 0;

    /**
     * Inverse mass
     */
    public double inverseMass = 0;

    /**
     * Point of collision
     */
    public Vector3d localContactPoint = Vector3d.ZERO;

    public Contact(Manifold manifold, BlockState blockA, BlockState blockB, AbstractContraptionRigidbody rigidbody) {
        this.manifold = manifold;
        this.blockA = blockA;
        this.blockB = blockB;
        this.rigidbody = rigidbody;
    }
}
