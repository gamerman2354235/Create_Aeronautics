package com.eriksonn.createaeronautics.physics.api;

import com.eriksonn.createaeronautics.physics.collision.shape.CollisionQuality;
import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;

import java.util.List;

/**
 * Interface for BlockEntities to have a custom collision shape on simulated contraptions.
 */
public interface ICustomCollisionShape {

    /**
     * Determines if this collision shape should take priority over quality setting changes.
     * @return True if this collision shape should take priority over the default collision shape generator.
     */
    boolean takesPriority(CollisionQuality quality);

    /**
     * Generates the collision shape for this block entity.
     * @param quality The quality of the collision detection.
     * @return The collision shape to be used for this block entity.
     */
    List<ICollisionShape> generateCollisionShape(CollisionQuality quality);

}
