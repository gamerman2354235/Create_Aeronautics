package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.blocks.propeller_bearing.PropellerBearingTileEntity;
import com.eriksonn.createaeronautics.dimension.AirshipDimensionManager;
import com.eriksonn.createaeronautics.index.CABlocks;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.simibubi.create.AllTags;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.feature.template.Template;

import java.util.*;


public class PhysicsManager {
    AirshipContraptionEntity entity;
    AirshipContraption contraption;
    Quaternion orientation;
    Vector3d momentum=Vector3d.ZERO;
    Vector3d centerOfMass=Vector3d.ZERO;
    Vector3d angularMomentum=Vector3d.ZERO;
    Vector3d angularVelocity=Vector3d.ZERO;
    double[][] localInertiaTensor=new double[3][3];
    Quaternion principalRotation;
    double[] principalInertia =new double[3];
    double mass;
    Vector3d localForce =Vector3d.ZERO;
    Vector3d globalForce =Vector3d.ZERO;
    Vector3d localTourqe =Vector3d.ZERO;
    Vector3d globalTourqe =Vector3d.ZERO;
    Vector3d globalVelocity=Vector3d.ZERO;
    Vector3d localVelocity=Vector3d.ZERO;
    double totalAccumulatedBuoyancy =0.0;
    final double[][][] LeviCivitaTensor;

    final double dt=0.05f;//converts the time unit to seconds instead of ticks
    final double gravity=5.00;// m/s^2
    BuoyancyController levititeBuoyancyController=new BuoyancyController(6.0);
    boolean isInitialized=false;

    Vector3f CurrentAxis=new Vector3f(1,1,1);
    float CurrentAxisAngle = 0;


    public PhysicsManager(AirshipContraptionEntity entity)
    {
        orientation=Quaternion.ONE.copy();
        //orientation=new Quaternion(1,0,0,1);
        //orientation.normalize();

        this.entity=entity;
        momentum=Vector3d.ZERO;
        LeviCivitaTensor=new double[3][3][3];
        LeviCivitaTensor[0][1][2]=LeviCivitaTensor[2][0][1]=LeviCivitaTensor[1][2][0]=1;
        LeviCivitaTensor[2][1][0]=LeviCivitaTensor[0][2][1]=LeviCivitaTensor[1][0][2]=-1;
        principalRotation =Quaternion.ONE.copy();
        //angularMomentum=new Vector3d(0,100,2);
        //principialRotation=new Quaternion(1,2,3,4);
        //principialRotation.normalize();
    }
    public void tryInit()
    {
        if(!isInitialized) {
            contraption=entity.airshipContraption;
            updateCenterOfMass();
            updateLevititeBuoyancy();
            isInitialized=true;
        }
    }
    public void tick()
    {
        contraption=entity.airshipContraption;

        if(contraption==null)
            return;
        updateCenterOfMass();
        tryInit();

        //orientation=new Quaternion(0,0,0.3827f,0.9239f);
        //orientation.normalize();



        updateWings();
        updateInertia();
        updateTileEntityInteractions();
        totalAccumulatedBuoyancy =0;

        totalAccumulatedBuoyancy += levititeBuoyancyController.apply(orientation,entity.position());
        updateRotation();

        globalForce=globalForce.add(0,-totalAccumulatedBuoyancy,0);

        momentum = momentum.add(rotateQuat(localForce.scale(dt),orientation)).add(globalForce.scale(dt));
        globalForce = Vector3d.ZERO;
        localForce = Vector3d.ZERO;

        momentum=momentum.scale(0.995);
        globalVelocity=momentum.scale(1.0/mass);
        localVelocity = rotateQuatReverse(globalVelocity,orientation);

        float c = (float)Math.cos(CurrentAxisAngle);
        float s = (float)Math.sin(CurrentAxisAngle);
        CurrentAxis=new Vector3f(c,3,s);
        CurrentAxis=new Vector3f(0,1,0);
        CurrentAxis.normalize();


        CurrentAxisAngle+=0.01f;
//        orientation=new Quaternion(s*CurrentAxis.x(),s*CurrentAxis.y(),s*CurrentAxis.z(),c);

        entity.quat=orientation.copy();
        entity.velocity=globalVelocity.scale(dt);
        entity.setDeltaMovement(globalVelocity.scale(dt));
        entity.move(globalVelocity.x*dt,globalVelocity.y*dt,globalVelocity.z*dt);
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
                Vector3d pos = getLocalCoordinate(entry.getKey());
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
    void updateCenterOfMass()
    {
        //Center of mass is the weighted average of the block positions, weighted by their mass
        mass=0;
        centerOfMass=Vector3d.ZERO;
        for (Map.Entry<BlockPos, Template.BlockInfo> entry : contraption.getBlocks().entrySet()) {
            if (!entry.getValue().state.isAir()) {
                double blockMass=getBlockMass(entry.getValue());
                Vector3d pos=new Vector3d(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ());
                centerOfMass=centerOfMass.add(pos.scale(blockMass));
                mass+=blockMass;
            }
        }
        // for every subcontraption, add its center of mass
        for (Map.Entry<UUID, ControlledContraptionEntity> contraptionEntry : entity.subContraptions.entrySet()) {
            for (Map.Entry<BlockPos, Template.BlockInfo> entry : contraptionEntry.getValue().getContraption().getBlocks().entrySet()) {
                if (!entry.getValue().state.isAir()) {
                    double blockMass=getBlockMass(entry.getValue());
                    Vector3d pos=new Vector3d(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ());

                    Vector3d anchorVec = contraptionEntry.getValue().getAnchorVec();
                    BlockPos plotPos = AirshipManager.getPlotPosFromId(entity.plotId);
                    if(entity.level.isClientSide) {
                        anchorVec = anchorVec.subtract(0, plotPos.getY(), 0);
                    } else {
                        anchorVec = anchorVec.subtract(plotPos.getX(), plotPos.getY(), plotPos.getZ());
                    }

                    pos=anchorVec.add(contraptionEntry.getValue().applyRotation(pos, 1.0f));
                    centerOfMass=centerOfMass.add(pos.scale(blockMass));
                    mass+=blockMass;
                }
            }
        }

        centerOfMass=centerOfMass.scale(1.0/mass);
        entity.centerOfMassOffset=centerOfMass;
    }
    double getBlockMass(Template.BlockInfo info)
    {
        if(info.state.is(BlockTags.WOOL))
        {
            return 0.2;
        }
        if(AllTags.AllBlockTags.WINDMILL_SAILS.matches(info.state))
        {
            return 0.2;
        }
        return 1.0;
    }
    void updateInertia()
    {

        localInertiaTensor=new double[3][3];

        for (Map.Entry<BlockPos, Template.BlockInfo> entry : contraption.getBlocks().entrySet())
        {
            if(!entry.getValue().state.isAir())
            {
                double blockMass=getBlockMass(entry.getValue());
                Vector3d pos=getLocalCoordinate(entry.getKey());
                double[] posArray=new double[]{pos.x,pos.y,pos.z};

                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        localInertiaTensor[i][j]-=blockMass*posArray[i]* posArray[j];
                for (int i = 0; i < 3; i++) localInertiaTensor[i][i] += blockMass * pos.lengthSqr();
            }
        }
    }
    void updateRotation()
    {
        //Find the eigenvector decomposition of the inertia tensor
        //in terms of principal components and a quaternion rotation
        updateSpectralDecomposition();

        //column vectors of the original inertia tensor
        Vector3d InertiaTensorI=new Vector3d(localInertiaTensor[0][0],localInertiaTensor[0][1],localInertiaTensor[0][2]);
        Vector3d InertiaTensorJ=new Vector3d(localInertiaTensor[1][0],localInertiaTensor[1][1],localInertiaTensor[1][2]);
        Vector3d InertiaTensorK=new Vector3d(localInertiaTensor[2][0],localInertiaTensor[2][1],localInertiaTensor[2][2]);

        //decomposition into principal components
        principalInertia[0]=rotateQuat(InertiaTensorI, principalRotation).length();
        principalInertia[1]=rotateQuat(InertiaTensorJ, principalRotation).length();
        principalInertia[2]=rotateQuat(InertiaTensorK, principalRotation).length();
        Vector3d inversePrincipialInertia = new Vector3d(
                1/ principalInertia[0],
                1/ principalInertia[1],
                1/ principalInertia[2]);

        //global tourqe to local reference frame
        localTourqe=localTourqe.add(rotateQuatReverse(globalTourqe,orientation));

        angularMomentum=angularMomentum.add(localTourqe.scale(dt));
        double momentumMag = angularMomentum.length();
        angularVelocity = rotateQuat(angularMomentum, principalRotation).multiply(inversePrincipialInertia);

        Vector3d angularAcceleration = new Vector3d(
                (principalInertia[2] - principalInertia[1]) * angularVelocity.y * angularVelocity.z,
                (principalInertia[0] - principalInertia[2]) * angularVelocity.z * angularVelocity.x,
                (principalInertia[1] - principalInertia[0]) * angularVelocity.x * angularVelocity.y
        );

        angularVelocity = angularVelocity.add(angularAcceleration.multiply(inversePrincipialInertia.scale(dt)));

        angularVelocity = rotateQuatReverse(angularVelocity, principalRotation);

        angularMomentum = angularMomentum.add(rotateQuatReverse(angularAcceleration.scale(dt), principalRotation));

        if (angularMomentum.lengthSqr() > 0)//reset the length to maintain conservation of momentum
        {
            angularMomentum=angularMomentum.normalize();
            angularMomentum=angularMomentum.scale(momentumMag);

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
        // attempts to perform spectral decomposition on the local inertia tensor = A
        // this is done by finding a rotation Q such that D = (Q^-1)AQ is a diagonal matrix
        // the way this is done is by starting with some attempted matrix M = (Q^-1)AQ using the current rotation Q
        // and then trying to find a small pertubation rotation dQ that causes the resulting matrix (dQ^-1)MdQ
        // to become more diagonal than what M currently is
        // dQ is defined using a small cross product dQ(v) = v + cross(v,k)

        //scaleDown parameter is used to scale down the matrix to have approximatly unit length column vectors
        //somehow this makes the algorithm far more stable
        double scaleDown = 0;
        for (int i =0;i<3;i++)
        {
            for (int j =0;j<3;j++)
            {
                scaleDown+=localInertiaTensor[i][j]*localInertiaTensor[i][j];
            }
        }
        scaleDown=Math.sqrt(scaleDown);

        // attemptedDecomposition is the attempted result matrix using the current rotation
        // attemptedDecomposition = rotationInverse * localInertiaTensor * rotation
        // the goal is to get this to be a diagonal matrix, as then the principialRotation (if expressed as a matrix)
        // will have the principial axies as column vectors,
        // and the resulting diagonal matrix will contain the moments of inertia
        double[][] attemptedDecomposition = new double[3][3];
        for (int i =0;i<3;i++)
        {
            Vector3d v=setVectorFromIndex(i,1);

            v=rotateQuat(v, principalRotation);
            v=multiplyMatrixArray(localInertiaTensor,v);
            v=rotateQuatReverse(v, principalRotation);

            attemptedDecomposition[i][0]=v.x/scaleDown;
            attemptedDecomposition[i][1]=v.y/scaleDown;
            attemptedDecomposition[i][2]=v.z/scaleDown;
        }
        // numerically that goal corresponds to this cost value being as low as possible
        // as this cost is the sum of the squares of the non-diagonal elements
        // so a diagonal matrix will have zero cost
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

        double minCost=0.01;
        double stepScale=0.2;
        if(cost<minCost||gradientVector.lengthSqr()==0)
            return;
        //newtons method
        Vector3d change = gradientVector.scale(stepScale*(cost-minCost)/gradientVector.lengthSqr());

        //System.out.println("Cost: "+cost);
        Quaternion q=new Quaternion((float)change.x,(float)change.y,(float)change.z,1.0f);
        principalRotation.mul(q);
        principalRotation.normalize();
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
    void updateWings()
    {
        for (Map.Entry<BlockPos, BlockState> entry : entity.sails.entrySet())
        {
            Vector3d pos = getLocalCoordinate(entry.getKey());
            Vector3d vel = getLocalVelocityAtPosition(pos);
            Vector3d normal = getFacingVector(entry.getValue());
            Vector3d force = normal.scale(-0.8f*normal.dot(vel));
            addForce(force,pos);
        }
        for (Map.Entry<UUID, ControlledContraptionEntity> contraptionEntityEntry : entity.subContraptions.entrySet())
        {
            // TODO: make propellers not provide lift
            if(contraptionEntityEntry.getValue() != null)
            {
                Contraption subContraption = contraptionEntityEntry.getValue().getContraption();

                for (Map.Entry<BlockPos, Template.BlockInfo> blockStateEntry : subContraption.getBlocks().entrySet())
                {
                    if(blockStateEntry.getValue().state.getBlock() instanceof SailBlock) {

                        Vector3d pos = contraptionEntityEntry.getValue().applyRotation(VecHelper.getCenterOf(blockStateEntry.getKey()),0);
                        pos.subtract(centerOfMass);
                        Vector3d vel = getLocalVelocityAtPosition(pos);
                        Vector3d normal = getFacingVector(blockStateEntry.getValue().state);
                        normal = contraptionEntityEntry.getValue().applyRotation(normal,0);
                        Vector3d force = normal.scale(-0.8f*normal.dot(vel));
                        addForce(force,pos);

                    }
                }
            }
        }
    }
    void updateTileEntityInteractions()
    {
        Vector3d TotalForce=Vector3d.ZERO;
        for (Map.Entry<BlockPos, TileEntity> entry : contraption.presentTileEntities.entrySet())
        {
            TileEntity te = entry.getValue();
            BlockPos pos = entry.getKey();
            Vector3d posV = getLocalCoordinate(entry.getKey());
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
    Vector3d getLocalVelocityAtPosition(Vector3d pos)
    {
        return localVelocity.add(pos.cross(angularVelocity));
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
        float magnitude = 0.5f*te.getSpeed();
        return direction.scale(magnitude);
    }

    Vector3d getFacingVector(BlockState state)
    {
        Direction direction = state.getValue(BlockStateProperties.FACING);
        return new Vector3d(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
    }
    Vector3d getLocalCoordinate(BlockPos pos)
    {
        Vector3d p=new Vector3d(pos.getX(),pos.getY(),pos.getZ());
        return p.subtract(centerOfMass);

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
            /*
            calculates the total buoyancy force and point of average force application
            average force application is the weighted average of the positions,
            with the weights being the buoyancy forces at the worldpositions of each point
            weight = gravity*getAirPressure(worldPos)*strengthScale
            total buoyancy is then the sum of all those weights
            */

            double referenceBuoyancy = gravity*getAirPressure(referencePos)*strengthScale;
            double referenceBuoyancyDerivative = gravity*getAirPressureDerivative(referencePos)*strengthScale;
            Vector3d rotatedAverage = rotateQuat(averagePos,rotation);
            double averageBuoyancy = referenceBuoyancy + rotatedAverage.dot(upVector) * referenceBuoyancyDerivative;
            double totalBuoyancy = averageBuoyancy*totalCount;

            Vector3d circleCenter = upVector.scale(averageSquaredMagnitudes*0.5);
            Vector3d circularOffset =projectedAveragePos.subtract(circleCenter);
            circularOffset = circleCenter.add(rotateQuat(rotateQuat(circularOffset,rotation),rotation));

            Vector3d averageBuoyancyPosition =
                    (rotatedAverage.scale(referenceBuoyancy)
                    .add(circularOffset.scale(referenceBuoyancyDerivative)))
                    .scale(1.0/averageBuoyancy);
            //averageBuoyancyPosition=rotatedAverage;

            //addForce(rotateQuatReverse(upVector,rotation).scale(totalBuoyancy),averagePos);
            addGlobalForce(upVector.scale(totalBuoyancy),averageBuoyancyPosition);

            return totalBuoyancy;

        }
    }

    /**
     * Converts a vector to an array
     */
    public double[] vecToArray(Vector3d v)
    {
        return new double[]{v.x,v.y,v.z};
    }

    /**
     * Converts an array to a vector
     */
    public Vector3d arrayToVec(double[] array)
    {
        return new Vector3d(array[0],array[1],array[2]);
    }

    /**
     * Converts a quaternion to an array
     */
    public double[] quatToArray(Quaternion q)
    {
        return new double[]{q.i(),q.j(),q.k(),q.r()};
    }

    /**
     * Converts an array to a quaternion
     */
    public Quaternion arrayToQuat(double[] array)
    {
        return new Quaternion((float)array[0], (float)array[1], (float)array[2], (float)array[3]);
    }

}
