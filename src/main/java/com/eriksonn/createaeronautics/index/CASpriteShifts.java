package com.eriksonn.createaeronautics.index;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;

import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.OMNIDIRECTIONAL;

public class CASpriteShifts {

    public static final CTSpriteShiftEntry LEVITITE_CASING = omni("block/levitite_casing");

    static CTSpriteShiftEntry omni(String name) {
        return CTSpriteShifter.getCT(OMNIDIRECTIONAL,
                CreateAeronautics.asResource(name),
                CreateAeronautics.asResource(name + "_connected"));
    }

}
