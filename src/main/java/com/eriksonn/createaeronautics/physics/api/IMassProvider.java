package com.eriksonn.createaeronautics.physics.api;

/**
 * Phyiscs-related interface for BlockEntities with variable mass.
 */
public interface IMassProvider {

    /**
     * Gets the current of the object.
     */
    public double getMass();

}
