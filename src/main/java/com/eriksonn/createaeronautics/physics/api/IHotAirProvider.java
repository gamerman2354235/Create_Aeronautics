package com.eriksonn.createaeronautics.physics.api;

/**
 * Phyiscs-related interface for BlockEntities that can produce hot air for hot air balloons.
 */
public interface IHotAirProvider {

    /**
     * Gets the current hot air output of the object.
     */
    double getHotAirOutput();

}
