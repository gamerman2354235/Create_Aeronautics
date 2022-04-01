package com.eriksonn.createaeronautics.connected;


import com.eriksonn.createaeronautics.CreateAeronautics;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import net.minecraft.util.ResourceLocation;

public class CTSpriteShifter  extends SpriteShifter {
    public static CTSpriteShiftEntry getCT(com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType type, ResourceLocation blockTexture, ResourceLocation connectedTexture) {
        String key = type.name() + ":" + blockTexture + "->" + connectedTexture;
        if (ENTRY_CACHE.containsKey(key))
            return (CTSpriteShiftEntry) ENTRY_CACHE.get(key);

        CTSpriteShiftEntry entry = create(type);
        entry.set(blockTexture, connectedTexture);
        ENTRY_CACHE.put(key, entry);
        return entry;
    }

    public static CTSpriteShiftEntry getCT(com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType type, String blockTextureName, String connectedTextureName) {
        return getCT(type, CreateAeronautics.asResource("block/" + blockTextureName), CreateAeronautics.asResource("block/" + connectedTextureName + "_connected"));
    }

    public static CTSpriteShiftEntry getCT(com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }

    private static CTSpriteShiftEntry create(com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType type) {
        switch (type) {
            case HORIZONTAL:
                return new CTSpriteShiftEntry.Horizontal();
            case OMNIDIRECTIONAL:
                return new CTSpriteShiftEntry.Omnidirectional();
            case VERTICAL:
                return new CTSpriteShiftEntry.Vertical();
            case CROSS:
                return new CTSpriteShiftEntry.Cross();
            default:
                return null;
        }
    }

    public enum CTType {
        OMNIDIRECTIONAL, HORIZONTAL, VERTICAL, CROSS;
    }

}
