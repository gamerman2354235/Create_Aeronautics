package com.eriksonn.createaeronautics.index;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;

import static com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType.OMNIDIRECTIONAL;
import static com.eriksonn.createaeronautics.connected.CTSpriteShifter.getCT;

public class CASpriteShifts {

    public static final CTSpriteShiftEntry LEVITITE_CASING = omni("levitite_casing");

    static CTSpriteShiftEntry omni(String name) {
        return getCT(OMNIDIRECTIONAL, name);
    }

}
