package com.eriksonn.createaeronautics.physics.collision.detection;

import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.SimulatedContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.collision.resolution.IIterativeManifoldSolver;

import java.util.List;

/**
 * Interface for collision detection methods, intended for use with contraption rigidbodies.
 */
public interface ICollisionDetector {

    /**
     * Detects collisions between a contraption rigidbody and the world.
     * @param rb the contraption rigidbody to check for collisions with the world.
     * @param contacts the existing list of contacts to mutate.
     * @return the mutated list of contacts.
     */
    List<Contact> solve(AbstractContraptionRigidbody rb, List<Contact> contacts);

}
