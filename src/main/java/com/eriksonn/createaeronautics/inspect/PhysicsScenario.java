package com.eriksonn.createaeronautics.inspect;

import com.eriksonn.createaeronautics.index.CABlocks;
import com.eriksonn.createaeronautics.index.CAParticleTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.repack.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.util.IItemProvider;

public enum PhysicsScenario {
    BOUYANCY("Test the bouyancy of your contraption", AllBlocks.WATER_WHEEL),
    BALANCE("Test the balance of your contraption", AllBlocks.TURNTABLE),
    WIND_TUNNEL("Test the aerodynamics of your contraption", AllBlocks.SAIL),
    NONE;
    public String text;
    public BlockEntry item = CABlocks.AIRSHIP_ASSEMBLER;

    PhysicsScenario() {
        this.text = "";
    }

    PhysicsScenario(String text) {
        this.text = text;
    }

    PhysicsScenario(String text, BlockEntry item) {
        this.text = text;
        this.item = item;

    }
}
