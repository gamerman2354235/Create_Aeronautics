package com.eriksonn.createaeronautics.physics;

import com.eriksonn.createaeronautics.index.CAConfig;
import com.eriksonn.createaeronautics.index.CATags;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;

public class PhysicsUtils {

    public static double[][][] LeviCivitaTensor;
    public final static double deltaTime=0.05f;//converts the time unit to seconds instead of ticks
    public final static double gravity=5.00;// m/s^2

    public static void generateLeviCivitaTensor()
    {
        LeviCivitaTensor=new double[3][3][3];
        LeviCivitaTensor[0][1][2]=LeviCivitaTensor[2][0][1]=LeviCivitaTensor[1][2][0]=1;
        LeviCivitaTensor[2][1][0]=LeviCivitaTensor[0][2][1]=LeviCivitaTensor[1][0][2]=-1;
    }

    public static double getBlockMass(Template.BlockInfo info)
    {
        if (info.state.is(CATags.LIGHT))
        {
            return CAConfig.LIGHT_BLOCK_WEIGHT.get();
        }
        return CAConfig.DEFAULT_BLOCK_WEIGHT.get();
    }


    private static final double scaleHeight=128.0;// exponential decay rate
    private static final double worldHeight=256.0;// pressure at world height = 0
    private static final double referenceHeight=64.0;// pressure at sea level = 1
    private static final double worldHeightPressureOffset = Math.exp(-(worldHeight-referenceHeight)/scaleHeight);
    //multiplier for buoyancy and propeller efficiency
    public static double getAirPressure(Vector3d pos)
    {
        double height = pos.y;
        return (Math.exp(-(height-referenceHeight)/scaleHeight)-worldHeightPressureOffset)/(1.0-worldHeightPressureOffset);
    }
    public static double getAirPressureDerivative(Vector3d pos)
    {
        double height = pos.y;
        return -(Math.exp(-(height-referenceHeight)/scaleHeight))/(scaleHeight*(1.0-worldHeightPressureOffset));
    }
}
