package com.eriksonn.createaeronautics.physics.collision.detection;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Represents a result for simplex evolution.
 */
public class EvolutionResult {
    public boolean complete;
    public Vector3d direction;

    public EvolutionResult(boolean complete, Vector3d direction) {
        this.complete = complete;
        this.direction = direction;
    }
}
