package com.eriksonn.createaeronautics.utils;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import net.minecraft.util.math.vector.Vector3d;

public interface AbstractContraptionEntityExtension {
    public void createAeronautics$setOriginalPosition(Vector3d pos);
    public Vector3d createAeronautics$getOriginalPosition();
}
