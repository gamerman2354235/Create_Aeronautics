package com.eriksonn.createaeronautics.index;

import com.eriksonn.createaeronautics.CreateAeronautics;
import com.eriksonn.createaeronautics.physics.collision.shape.CollisionQuality;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Contains all the config stuff
 * @author FortressNebula
 */
public class CAConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final String SERVER_FILENAME = CreateAeronautics.MODID + "-server.toml";

    public static final String PHYSICS_CATEGORY = "Physics";
    public static final ForgeConfigSpec.EnumValue<CollisionQuality> COLLISION_QUALITY;
    public static final ForgeConfigSpec.IntValue MAX_COLLISION_ITERATIONS;
    public static final ForgeConfigSpec.DoubleValue LIGHT_BLOCK_WEIGHT;
    public static final ForgeConfigSpec.DoubleValue DEFAULT_BLOCK_WEIGHT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push(PHYSICS_CATEGORY);
        COLLISION_QUALITY = builder
                .comment("What quality of collision detection should simulated contraptions use?")
                .defineEnum("collisionQuality", CollisionQuality.GJKEPA);
        MAX_COLLISION_ITERATIONS = builder
                .comment("How many iterations should the collision engine be allowed run per tick?")
                .defineInRange("maxIterations", 3, 1, 20);
        LIGHT_BLOCK_WEIGHT = builder
                .comment("Some blocks have the 'light' tag and will be given less weight than others. How much should these weigh?")
                .defineInRange("lightBlockWeight", 0.2, 0, Double.MAX_VALUE);
        DEFAULT_BLOCK_WEIGHT = builder
                .comment("How much should ordinary blocks weigh?")
                .defineInRange("defaultBlockWeight", 1.0, 0, Double.MAX_VALUE);
        builder.pop();

        SERVER_CONFIG = builder.build();
    }
}
