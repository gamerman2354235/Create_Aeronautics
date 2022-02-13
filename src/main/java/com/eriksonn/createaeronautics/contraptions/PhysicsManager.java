package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.blocks.propeller_bearing.PropellerBearingTileEntity;
import com.eriksonn.createaeronautics.index.CABlocks;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PhysicsManager {
    AirshipContraptionEntity entity;
    AirshipContraption contraption;
    private Quaternion orientation;
    Vector3d momentum=Vector3d.ZERO;
    Vector3d centerOfMass=Vector3d.ZERO;
    private Vector3d angularMomentum=Vector3d.ZERO;
    Vector3d angularVelocity=Vector3d.ZERO;
    double[][] localInertiaTensor=new double[3][3];
    Quaternion principialRotation;
    double[] principialInertia=new double[3];
    double mass;
    Vector3d localForce =Vector3d.ZERO;
    Vector3d globalForce =Vector3d.ZERO;
    Vector3d localTourqe =Vector3d.ZERO;
    Vector3d globalTourqe =Vector3d.ZERO;
    double totalAccumulatedBuoyancy =0.0;
    final double[][][] LeviCivitaTensor;

    final double dt=0.05f;//converts the time unit to seconds instead of ticks
    final double gravity=5.00;// m/s^2
    BuoyancyController levititeBuoyancyController=new BuoyancyController(1.0);


    public PhysicsManager(AirshipContraptionEntity entity)
    {
        orientation=Quaternion.ONE.copy();
        //orientation=new Quaternion(1,0,0,1);
        //orientation.normalize();
        contraption=entity.airshipContraption;
        this.entity=entity;
        momentum=Vector3d.ZERO;
        LeviCivitaTensor=new double[3][3][3];
        LeviCivitaTensor[0][1][2]=LeviCivitaTensor[2][0][1]=LeviCivitaTensor[1][2][0]=1;
        LeviCivitaTensor[2][1][0]=LeviCivitaTensor[0][2][1]=LeviCivitaTensor[1][0][2]=-1;
        principialRotation=Quaternion.ONE.copy();

    }
    public void init()
    {
        updateLevititeBuoyancy();
    }
    public void tick()
    {
        //orientation=new Quaternion(0,0,0.3827f,0.9239f);
        //orientation.normalize();


        contraption=entity.airshipContraption;
        if(contraption==null)
            return;
        updateInertia();
        updateTileEntityInteractions();
        totalAccumulatedBuoyancy =0;

        totalAccumulatedBuoyancy += levititeBuoyancyController.apply(orientation,entity.position());
        updateRotation();

        globalForce=globalForce.add(0,-totalAccumulatedBuoyancy,0);

        momentum = momentum.add(rotateQuat(localForce,orientation)).add(globalForce);
        globalForce = Vector3d.ZERO;
        localForce = Vector3d.ZERO;

        momentum=momentum.scale(0.995);
        Vector3d velocity=momentum.scale(dt/mass);
        entity.quat=orientation.copy();
        entity.velocity=velocity.scale(dt);
        entity.setDeltaMovement(velocity.scale(dt));
        entity.move(velocity.x*dt,velocity.y*dt,velocity.z*dt);
    }
    protected void readAdditional(CompoundNBT compound, boolean spawnPacket) {

        orientation = readQuaternion(compound.getCompound("Orientation"));
        momentum = readVector(compound.getCompound("Momentum"));
        angularMomentum = readVector(compound.getCompound("AngularMomentum"));
    }

    protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
        compound.put("Orientation",writeQuaternion(orientation));
        compound.put("Momentum",writeVector(momentum));
        compound.put("AngularMomentum",writeVector(angularMomentum));
    }
    CompoundNBT writeQuaternion(Quaternion Q)
    {
        CompoundNBT compound=new CompoundNBT();
        compound.putFloat("R",Q.r());
        compound.putFloat("I",Q.i());
        compound.putFloat("J",Q.j());
        compound.putFloat("K",Q.k());
        return compound;
    }
    Quaternion readQuaternion(CompoundNBT compound)
    {
        float r = compound.getFloat("R");
        float i = compound.getFloat("I");
        float j = compound.getFloat("J");
        float k = compound.getFloat("K");
        return new Quaternion(i,j,k,r);
    }
    CompoundNBT writeVector(Vector3d V)
    {
        CompoundNBT compound=new CompoundNBT();
        compound.putDouble("X",V.x);
        compound.putDouble("Y",V.y);
        compound.putDouble("Z",V.z);
        return compound;
    }
    Vector3d readVector(CompoundNBT compound)
    {
        double x = compound.getDouble("X");
        double y = compound.getDouble("Y");
        double z = compound.getDouble("Z");
        return new Vector3d(x,y,z);
    }
    public Quaternion getPartialOrientation(float partialTick)
    {
        Vector3d v = angularVelocity.scale(partialTick*dt*0.5f);
        Quaternion q = new Quaternion((float)v.x,(float)v.y,(float)v.z, 1.0f);
        q.mul(orientation);
        q.normalize();
        return q;
    }
    public static Vector3d rotateQuat(Vector3d V,Quaternion Q)
    {
        Quaternion q=new Quaternion((float)V.x,(float)V.y,(float)V.z,0.0f);
        Quaternion Q2 = Q.copy();
        q.mul(Q2);
        Q2.conj();
        Q2.mul(q);
        return new Vector3d(Q2.i(),Q2.j(),Q2.k());
    }
    public static Vector3d rotateQuatReverse(Vector3d V,Quaternion Q)
    {
        Quaternion q=new Quaternion((float)V.x,(float)V.y,(float)V.z,0.0f);
        Quaternion Q2 = Q.copy();
        Q2.conj();
        q.mul(Q2);
        Q2.conj();
        Q2.mul(q);
        return new Vector3d(Q2.i(),Q2.j(),Q2.k());
    }
    void updateLevititeBuoyancy()
    {
        List<Vector3d> levititeBlocks=new ArrayList<>();
        for (Map.Entry<BlockPos, Template.BlockInfo> entry : entity.airshipContraption.getBlocks().entrySet())
        {
            if(entry.getValue().state == CABlocks.LEVITITE_CASING.getDefaultState())
            {
                Vector3d pos=new Vector3d(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ());
                levititeBlocks.add(pos);
            }
        }
        levititeBuoyancyController.set(levititeBlocks);
    }

    private static final double scaleHeight=128.0;// exponential decay rate
    private static final double worldHeight=256.0;// pressure at world height = 0
    private static final double referenceHeight=64.0;// pressure at sea level = 1
    private static final double worldHeightPressureOffset = Math.exp(-(worldHeight-referenceHeight)/scaleHeight);
    //multiplier for buoyancy and propeller efficiency
    public double getAirPressure(Vector3d pos)
    {
        double height = pos.y;
        return (Math.exp(-(height-referenceHeight)/scaleHeight)-worldHeightPressureOffset)/(1.0-worldHeightPressureOffset);
    }
    public double getAirPressureDerivative(Vector3d pos)
    {
        double height = pos.y;
        return -(Math.exp(-(height-referenceHeight)/scaleHeight))/(scaleHeight*(1.0-worldHeightPressureOffset));
    }
    void updateInertia()
    {
        mass=0;
        localInertiaTensor=new double[3][3];
        centerOfMass=Vector3d.ZERO;
        for (Map.Entry<BlockPos, Template.BlockInfo> entry : contraption.getBlocks().entrySet())
        {
            if(!entry.getValue().state.isAir())
            {
                float blockMass=1.0f;
                Vector3d pos=new Vector3d(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ());
                double[] posArray=new double[]{pos.x,pos.y,pos.z};

                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        localInertiaTensor[i][j]-=blockMass*posArray[i]* posArray[j];
                for (int i = 0; i < 3; i++) localInertiaTensor[i][i] += blockMass * pos.lengthSqr();

                centerOfMass=centerOfMass.add(pos);
                mass+=blockMass;
            }
        }
    }
    void updateRotation()
    {

        updateSpectralDecomposition();
        Vector3d InertiaTensorI=new Vector3d(localInertiaTensor[0][0],localInertiaTensor[0][1],localInertiaTensor[0][2]);
        Vector3d InertiaTensorJ=new Vector3d(localInertiaTensor[1][0],localInertiaTensor[1][1],localInertiaTensor[1][2]);
        Vector3d InertiaTensorK=new Vector3d(localInertiaTensor[2][0],localInertiaTensor[2][1],localInertiaTensor[2][2]);
        principialInertia[0]=rotateQuat(InertiaTensorI,principialRotation).length();
        principialInertia[1]=rotateQuat(InertiaTensorJ,principialRotation).length();
        principialInertia[2]=rotateQuat(InertiaTensorK,principialRotation).length();
        //principialRotation=Quaternion.ONE.copy();
        localTourqe=localTourqe.add(rotateQuatReverse(globalTourqe,orientation));
        //localTourqe = new Vector3d(0,1,0).scale(localTourqe.y);
        angularMomentum=angularMomentum.add(localTourqe.scale(dt));
        double momentumMag = angularMomentum.length();
        angularVelocity = rotateQuat(angularMomentum,principialRotation).multiply(1/principialInertia[0],1/principialInertia[1],1/principialInertia[2]);

        Vector3d angularAcceleration = new Vector3d(
                (principialInertia[2] - principialInertia[1]) * angularVelocity.y * angularVelocity.z,
                (principialInertia[0] - principialInertia[2]) * angularVelocity.z * angularVelocity.x,
                (principialInertia[1] - principialInertia[0]) * angularVelocity.x * angularVelocity.y
        );

        angularVelocity = angularVelocity.add(angularAcceleration.multiply(dt/principialInertia[0],dt/principialInertia[1],dt/principialInertia[2]));

        angularVelocity = rotateQuatReverse(angularVelocity,principialRotation);

        angularMomentum = angularMomentum.add(rotateQuatReverse(angularAcceleration.scale(dt),principialRotation));

        if (momentumMag > 0.01)//reset the length to maintain conservation of momentum
        {
            angularMomentum.normalize();
            angularMomentum.scale(momentumMag);
        }
        angularMomentum=angularMomentum.scale(0.998);
        Vector3d v = angularVelocity.scale(dt*0.5f);
        Quaternion q = new Quaternion((float)v.x,(float)v.y,(float)v.z, 1.0f);
        q.mul(orientation);
        orientation=q;
        orientation.normalize();

        localTourqe =Vector3d.ZERO;
        globalTourqe=Vector3d.ZERO;
    }
    void updateSpectralDecomposition()
    {
        double[][] attemptedDecomposition = new double[3][3];
        for (int i =0;i<3;i++)
        {
            Vector3d v=setVectorFromIndex(i,1);

            v=rotateQuat(v,principialRotation);
            v=multiplyMatrixArray(localInertiaTensor,v);
            v=rotateQuatReverse(v,principialRotation);

            attemptedDecomposition[i][0]=v.x;
            attemptedDecomposition[i][1]=v.y;
            attemptedDecomposition[i][2]=v.z;
        }
        double cost = 0;
        for (int i =0;i<3;i++)
        {
            for (int j =0;j<3;j++)
            {
                if(i!=j)
                    cost+=attemptedDecomposition[i][j]*attemptedDecomposition[i][j];
            }
        }

        Vector3d gradientVector=new Vector3d(0,0,0);
        for (int i =0;i<3;i++) {
            for (int j = 0; j < 3; j++) {
                if(i==j) continue;
                Vector3d v=new Vector3d(0,0,0);
                for (int k = 0; k < 3; k++)
                {
                    double scalar=0;
                    for(int r=0;r<3;r++)
                    {
                        if(k==r) continue;
                        scalar+=LeviCivitaTensor[k][i][r]*attemptedDecomposition[r][j];
                        scalar-=LeviCivitaTensor[k][r][j]*attemptedDecomposition[i][r];
                    }
                    v=setVectorFromIndex(k,scalar,v);
                }
                gradientVector=gradientVector.add(v.scale(2.0*attemptedDecomposition[i][j]));
            }
        }
        if(cost==0||gradientVector.lengthSqr()==0)
            return;
        //newtons method
        double minCost=1.0;
        double stepScale=0.2;
        Vector3d change = gradientVector.scale(stepScale*(cost-minCost)/gradientVector.lengthSqr());

        //System.out.println("Cost: "+cost);
        Quaternion q=new Quaternion((float)change.x,(float)change.y,(float)change.z,1.0f);
        principialRotation.mul(q);
        principialRotation.normalize();
    }
    Vector3d setVectorFromIndex(int i,double value)
    {
        double x=0,y=0,z=0;
        switch (i)
        {
            case(0):x=value;break;
            case(1):y=value;break;
            case(2):z=value;break;
        }
        return new Vector3d(x,y,z);
    }
    Vector3d setVectorFromIndex(int i,double value,Vector3d original)
    {
        double x=original.x,y=original.y,z=original.z;
        switch (i)
        {
            case(0):x+=value;break;
            case(1):y+=value;break;
            case(2):z+=value;break;
        }
        return new Vector3d(x,y,z);
    }

    double getIndexedVectorValue(int i,Vector3d V)
    {
        switch (i)
        {
            case(0):return V.x;
            case(1):return V.y;
            case(2):return V.z;
        }
        return 0;
    }
    Vector3d multiplyMatrixArray(double[][] M,Vector3d v)
    {
        Vector3d out = new Vector3d(0,0,0);
        for (int i =0;i<3;i++) {
            out=out.add(
            M[i][0]*getIndexedVectorValue(i,v),
            M[i][1]*getIndexedVectorValue(i,v),
            M[i][2]*getIndexedVectorValue(i,v)
            );
        }
        return out;
    }
    void updateTileEntityInteractions()
    {
        Vector3d TotalForce=Vector3d.ZERO;
        for (Map.Entry<BlockPos, TileEntity> entry : contraption.presentTileEntities.entrySet())
        {
            TileEntity te = entry.getValue();
            BlockPos pos = entry.getKey();
            Vector3d posV = new Vector3d(pos.getX(),pos.getY(),pos.getZ());
            if(CATileEntities.PROPELLER_BEARING.is(te))
                addForce(getForcePropellerBearing(pos,(PropellerBearingTileEntity)te),posV);
            if(AllTileEntities.ENCASED_FAN.is(te))
                addForce(getForceEncasedFan(pos,(EncasedFanTileEntity)te),posV);

        }
        momentum=momentum.add(TotalForce);
    }
    void addForce(Vector3d force, Vector3d pos)
    {
        localForce = localForce.add(force);
        localTourqe = localTourqe.add(force.cross(pos));
    }
    void addGlobalForce(Vector3d force, Vector3d pos)
    {
        globalForce = globalForce.add(force);
        globalTourqe = globalTourqe.add(force.cross(pos));
    }
    Vector3d getForcePropellerBearing(BlockPos pos,PropellerBearingTileEntity te)
    {
        if(!te.isRunning())
            return Vector3d.ZERO;

        Vector3d direction=getFacingVector(te.getBlockState());
        int sails = te.sailPositions.size();
        float magnitude = 0.2f*(float)Math.pow(sails,1.5f)*te.getAngularSpeed();
        if(te.movementDirection.getValue()==0)
            magnitude*=-1;
        return direction.scale(magnitude);
    }
    Vector3d getForceEncasedFan(BlockPos pos,EncasedFanTileEntity te)
    {

        Vector3d direction=getFacingVector(te.getBlockState());
        float magnitude = 0.1f*te.getSpeed();
        return direction.scale(magnitude);
    }

    Vector3d getFacingVector(BlockState state)
    {
        Direction direction = state.getValue(BlockStateProperties.FACING);
        return new Vector3d(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
    }
    class BuoyancyController
    {
        Vector3d averagePos;
        int totalCount;
        Vector3d upVector;
        Vector3d projectedAveragePos;
        double averageSquaredMagnitudes;
        double strengthScale=0.0;
        public BuoyancyController(double strengthScale)
        {
            this.strengthScale=strengthScale;
            upVector=new Vector3d(0,1,0);
        }
        public void set(List<Vector3d> points)
        {
            averagePos=Vector3d.ZERO;
            projectedAveragePos=Vector3d.ZERO;
            averageSquaredMagnitudes=0.0;
            totalCount=points.size();
            if(totalCount==0)
                return;
            for (Vector3d p:points) {
                averagePos=averagePos.add(p);
                projectedAveragePos=projectedAveragePos.add(p.scale(p.dot(upVector)));
                averageSquaredMagnitudes+=p.dot(p);
            }
            averagePos=averagePos.scale(1.0/totalCount);
            projectedAveragePos=projectedAveragePos.scale(1.0/totalCount);
            averageSquaredMagnitudes*=(1.0/totalCount);
        }
        public double apply(Quaternion rotation, Vector3d referencePos)
        {
            if(totalCount==0)
                return 0;
            double referenceBuoyancy = gravity*getAirPressure(referencePos)*strengthScale;
            double referenceBuoyancyDerivative = gravity*getAirPressureDerivative(referencePos)*strengthScale;
            Vector3d rotatedAverage = rotateQuat(averagePos,rotation);
            double averageBuoyancy = referenceBuoyancy + rotatedAverage.dot(upVector) * referenceBuoyancyDerivative;
            double totalBuoyancy = averageBuoyancy*totalCount;

            Vector3d circleCenter = upVector.scale(averageSquaredMagnitudes*0.5);
            Vector3d circularOffset = circleCenter.add(rotateQuat(rotateQuat(projectedAveragePos.subtract(circleCenter),rotation),rotation));

            Vector3d averageBuoyancyPosition = (rotatedAverage.scale(referenceBuoyancy).add(circularOffset.scale(referenceBuoyancyDerivative))).scale(1.0/averageBuoyancy);
            //return averageBuoyancyPosition.scale(totalCount);
            //averageBuoyancyPosition.scale(1.0/averageBuoyancy);
            addGlobalForce(upVector.scale(totalBuoyancy),averageBuoyancyPosition);

            return totalBuoyancy;

        }
    }

}
