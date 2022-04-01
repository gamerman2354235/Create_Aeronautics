package com.eriksonn.createaeronautics.physics.collision.resolution;

import com.eriksonn.createaeronautics.physics.IRigidbody;
import com.eriksonn.createaeronautics.physics.collision.detection.Contact;
import com.eriksonn.createaeronautics.physics.collision.detection.Manifold;
import net.minecraft.block.BlockState;

import java.util.List;

/**
 * Interface for iterative solvers of collision manifolds.
 */
public interface IIterativeManifoldSolver {

    /**
     * Initializes the solver for a new frame.
     * @param contacts the contacts
     */
    void preSolve(List<Contact> contacts);

    /**
     * Solves the collision contact.
     *
     * @param rigidbody the rigidbody
     * @param contacts  the contacts
     * @param dt        the time step
     */
    void solve(IRigidbody rigidbody, List<Contact> contacts, double dt);

    /**
     * Computes the restitution coefficient of a collision between two blocks on rigidbodies.
     */
    double getRestitution(BlockState blockA, BlockState blockB);
}
