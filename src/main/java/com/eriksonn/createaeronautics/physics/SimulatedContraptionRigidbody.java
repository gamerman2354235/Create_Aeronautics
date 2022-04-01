package com.eriksonn.createaeronautics.physics;


import com.eriksonn.createaeronautics.contraptions.AirshipContraption;
import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.contraptions.AirshipManager;
import com.eriksonn.createaeronautics.blocks.propeller_bearing.PropellerBearingTileEntity;
import com.eriksonn.createaeronautics.index.CABlocks;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.eriksonn.createaeronautics.mixins.ControlledContraptionEntityMixin;
import com.eriksonn.createaeronautics.particle.PropellerAirParticleData;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.gen.feature.template.Template;

import java.util.*;


public class SimulatedContraptionRigidbody extends AbstractContraptionRigidbody {
    AirshipContraptionEntity entity;
    AirshipContraption contraption;
    public Quaternion orientation;
    Vector3d momentum=Vector3d.ZERO;

    Vector3d centerOfMass=Vector3d.ZERO;
    double[][] inertiaTensor=new double[3][3];
    double mass;

    public Vector3d angularMomentum=Vector3d.ZERO;
    Vector3d angularVelocity=Vector3d.ZERO;
    public Quaternion principalRotation;
    public double[] principalInertia =new double[3];
    Vector3d localForce =Vector3d.ZERO;
    Vector3d globalForce =Vector3d.ZERO;
    Vector3d localTorque =Vector3d.ZERO;
    Vector3d globalTorque =Vector3d.ZERO;
    public Vector3d globalVelocity=Vector3d.ZERO;
    Vector3d localVelocity=Vector3d.ZERO;
    double totalAccumulatedBuoyancy =0.0;

    BuoyancyController levititeBuoyancyController=new BuoyancyController(6.0);
    boolean isInitialized=false;

    Vector3f CurrentAxis=new Vector3f(1,1,1);
    float CurrentAxisAngle = 0;
    public Map<UUID, SubcontraptionRigidbody> subcontraptionRigidbodyMap;

    Vector3d inertiaTensorI;
    Vector3d inertiaTensorJ;
    Vector3d inertiaTensorK;

    Vector3d inverseInertiaTensorI;
    Vector3d inverseInertiaTensorJ;
    Vector3d inverseInertiaTensorK;

    public SimulatedContraptionRigidbody(AirshipContraptionEntity entity)
    {
        orientation=Quaternion.ONE.copy();
        //orientation=new Quaternion(0,1,0,1);
        //orientation.normalize();

        this.entity=entity;
        momentum=Vector3d.ZERO;

        principalRotation =Quaternion.ONE.copy();
        //angularMomentum=new Vector3d(0,800,0);
        //principialRotation=new Quaternion(1,2,3,4);
        //principialRotation.normalize();
        PhysicsUtils.generateLeviCivitaTensor();
        subcontraptionRigidbodyMap=new HashMap<>();
    }
    public void tryInit()
    {
        if(!isInitialized) {
            contraption=entity.airshipContraption;

            generateMassDependentParameters(contraption,Vector3d.ZERO);
            mergeMassFromSubContraptions();

            updateLevititeBuoyancy();
            isInitialized=true;
            updateRotation();
            //applyImpulse(new Vector3d(0,0,1),new Vector3d(0,7,0));
            Vector3d V = getVelocityAtPoint(new Vector3d(0,0,1));
            Vector3d V2 = V.cross(V);
        }
    }
    public void tick()
    {
        contraption=entity.airshipContraption;

        if(contraption==null)
            return;
        generateMassDependentParameters(contraption,Vector3d.ZERO);

        mergeMassFromSubContraptions();

        //updateCenterOfMass();
        tryInit();

        //orientation=new Quaternion(0,0,0.3827f,0.9239f);
        //orientation.normalize();



        updateWings();
        //updateInertia();
        updateTileEntityInteractions();
        //centerOfMass=Vector3d.ZERO;
        totalAccumulatedBuoyancy =0;

        totalAccumulatedBuoyancy += levititeBuoyancyController.apply(orientation,entity.position());
        updateRotation();

        globalForce=globalForce.add(0,-totalAccumulatedBuoyancy,0);
        //globalForce=globalForce.add(0,-PhysicsUtils.gravity*mass,0);

        momentum = momentum.add(rotateQuat(localForce.scale(PhysicsUtils.deltaTime),orientation)).add(globalForce.scale(PhysicsUtils.deltaTime));
        globalForce = Vector3d.ZERO;
        localForce = Vector3d.ZERO;

        if(entity.position().y<75)
        {
            entity.move(0,75-entity.position().y,0);
            if(momentum.y<0)
            {
                momentum=momentum.multiply(1,-0.5,1);
            }
        }

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
        entity.velocity=globalVelocity.scale(PhysicsUtils.deltaTime);
        entity.setDeltaMovement(globalVelocity.scale(PhysicsUtils.deltaTime));
        entity.move(globalVelocity.x*PhysicsUtils.deltaTime,globalVelocity.y*PhysicsUtils.deltaTime,globalVelocity.z*PhysicsUtils.deltaTime);
    }

    public void readAdditional(CompoundNBT compound, boolean spawnPacket) {

        orientation = readQuaternion(compound.getCompound("Orientation"));
        momentum = readVector(compound.getCompound("Momentum"));
        angularMomentum = readVector(compound.getCompound("AngularMomentum"));
    }

    public void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
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
    public void addSubContraption(UUID uuid,AbstractContraptionEntity newEntity)
    {
        SubcontraptionRigidbody rigidbody = new SubcontraptionRigidbody(newEntity,this);
        subcontraptionRigidbodyMap.put(uuid, rigidbody);
        rigidbody.generateMassDependentParameters(newEntity.getContraption(),Vector3d.ZERO);
    }
    public void removeSubContraption(UUID uuid)
    {
        subcontraptionRigidbodyMap.remove(uuid);
    }
    public Quaternion getPartialOrientation(float partialTick)
    {
        Vector3d v = angularVelocity.scale(partialTick*PhysicsUtils.deltaTime*0.5f);
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

    void mergeMassFromSubContraptions()
    {
        mass=localMass;
        centerOfMass=localCenterOfMass.scale(mass);
        for(Map.Entry<UUID,SubcontraptionRigidbody> entry : subcontraptionRigidbodyMap.entrySet()) {
            SubcontraptionRigidbody rigidbody = entry.getValue();
            Vector3d entityOffsetPosition = rigidbody.entity.position().subtract(getPlotOffset());
            Vector3d pos = rigidbody.rotateLocal(rigidbody.localCenterOfMass).add(entityOffsetPosition);
            mass+=rigidbody.localMass;
            centerOfMass =centerOfMass.add(pos.scale(rigidbody.localMass));
        }
        centerOfMass = centerOfMass.scale(1/mass);

        inertiaTensor = localInertiaTensor.clone();
        Vector3d localShift = centerOfMass.subtract(localCenterOfMass);
        double[] posArray=new double[]{localShift.x,localShift.y,localShift.z};
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                inertiaTensor[i][j] -= localMass*posArray[i]* posArray[j];
        for (int i = 0; i < 3; i++) inertiaTensor[i][i] +=localMass * localShift.lengthSqr();


        for(Map.Entry<UUID,SubcontraptionRigidbody> entry : subcontraptionRigidbodyMap.entrySet())
        {
            SubcontraptionRigidbody rigidbody = entry.getValue();
            Vector3d entityOffsetPosition = rigidbody.entity.position().subtract(getPlotOffset());
            Vector3d pos = rigidbody.rotateLocal(rigidbody.localCenterOfMass).add(entityOffsetPosition);

            pos = pos.subtract(centerOfMass);

            posArray=new double[]{pos.x,pos.y,pos.z};
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    inertiaTensor[i][j]+= rigidbody.localInertiaTensor[i][j] - rigidbody.localMass*posArray[i]* posArray[j];
            for (int i = 0; i < 3; i++) inertiaTensor[i][i] +=rigidbody.localMass * pos.lengthSqr();
        }

        entity.centerOfMassOffset=centerOfMass;
    }
    void updateRotation()
    {
        //Find the eigenvector decomposition of the inertia tensor
        //in terms of principal components and a quaternion rotation


        //column vectors of the original inertia tensor
        inertiaTensorI=new Vector3d(inertiaTensor[0][0],inertiaTensor[0][1],inertiaTensor[0][2]);
        inertiaTensorJ=new Vector3d(inertiaTensor[1][0],inertiaTensor[1][1],inertiaTensor[1][2]);
        inertiaTensorK=new Vector3d(inertiaTensor[2][0],inertiaTensor[2][1],inertiaTensor[2][2]);

        //decomposition into principal components
        Vector3d principalVectorI = getPrincipalComponent(0);
        Vector3d principalVectorJ = getPrincipalComponent(1);
        Vector3d principalVectorK = getPrincipalComponent(2);
        principalInertia[0]=principalVectorI.length();
        principalInertia[1]=principalVectorJ.length();
        principalInertia[2]=principalVectorK.length();

        double determinant = principalVectorI.dot(principalVectorJ.cross(principalVectorK));

        inverseInertiaTensorI = inertiaTensorJ.cross(inertiaTensorK).scale(1/determinant);
        inverseInertiaTensorJ = inertiaTensorK.cross(inertiaTensorI).scale(1/determinant);
        inverseInertiaTensorK = inertiaTensorI.cross(inertiaTensorJ).scale(1/determinant);

        updateSpectralDecomposition();

        //global torque to local reference frame
        localTorque = localTorque.add(rotateQuatReverse(globalTorque,orientation));

        //torque gives a change of angular momentum over time
        angularMomentum=angularMomentum.add(localTorque.scale(PhysicsUtils.deltaTime));
        double momentumMag = angularMomentum.length();
        //rotate the angular momentum into the principal reference frame and scale by the inverse of the inertia
        //tensor to get angular velocity in the principal frame
        Vector3d principalVelocity = rotateQuat(multiplyInertiaInverse(angularMomentum), principalRotation);

        //euler's rotation equations
        Vector3d principalTorque = new Vector3d(
                (principalInertia[2] - principalInertia[1]) * principalVelocity.y * principalVelocity.z,
                (principalInertia[0] - principalInertia[2]) * principalVelocity.z * principalVelocity.x,
                (principalInertia[1] - principalInertia[0]) * principalVelocity.x * principalVelocity.y
        );

        //rotate the torque back to the contraption grid
        Vector3d extraTorque = rotateQuatReverse(principalTorque, principalRotation);

        angularMomentum = angularMomentum.add(extraTorque.scale(PhysicsUtils.deltaTime));

        angularVelocity=multiplyInertiaInverse(angularMomentum);

        if (angularMomentum.lengthSqr() > 0)//reset the length to maintain conservation of momentum
        {
            angularMomentum=angularMomentum.normalize();
            angularMomentum=angularMomentum.scale(momentumMag);

        }
        angularMomentum=angularMomentum.scale(0.995);
        Vector3d v = angularVelocity.scale(PhysicsUtils.deltaTime*0.5f);
        Quaternion q = new Quaternion((float)v.x,(float)v.y,(float)v.z, 1.0f);
        q.mul(orientation);
        orientation=q;
        orientation.normalize();

        localTorque =Vector3d.ZERO;
        globalTorque =Vector3d.ZERO;
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

            Vector3d v=getPrincipalComponent(i);

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
                        scalar+=PhysicsUtils.LeviCivitaTensor[k][i][r]*attemptedDecomposition[r][j];
                        scalar-=PhysicsUtils.LeviCivitaTensor[k][r][j]*attemptedDecomposition[i][r];
                    }
                    v=setVectorFromIndex(k,scalar,v);
                }
                gradientVector=gradientVector.add(v.scale(2.0*attemptedDecomposition[i][j]));
            }
        }

        double minCost=0.000000001;
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
    Vector3d getPrincipalComponent(int column)
    {
        Vector3d v=setVectorFromIndex(column,1);
        v=rotateQuat(v, principalRotation);
        v=multiplyInertia(v);
        v=rotateQuatReverse(v, principalRotation);
        return v;
    }
    /*void blockAddedEvent(BlockPos blockPos,Template.BlockInfo info)
    {
        LocalCenterOfMass = (LocalCenterOfMass * LocalMass + (Vector3)(Pos) * B.mass) / (LocalMass + B.mass);
        LocalMass += B.mass;
        double blockMass=getBlockMass(info);
        Vector3d pos=getLocalCoordinate(blockPos);
        double[] posArray=new double[]{pos.x,pos.y,pos.z};

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                localInertiaTensor[i][j]-=blockMass*posArray[i]* posArray[j];
        for (int i = 0; i < 3; i++) localInertiaTensor[i][i] += blockMass * pos.lengthSqr();

    }
    void blockRemovedEvent(BlockPos blockPos,Template.BlockInfo info)
    {
        LocalCenterOfMass = (LocalCenterOfMass * LocalMass - (Vector3)(Pos) * B.mass) / (LocalMass - B.mass);
        LocalMass -= B.mass;
        double blockMass=getBlockMass(info);
        Vector3d pos=getLocalCoordinate(blockPos);
        double[] posArray=new double[]{pos.x,pos.y,pos.z};

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                localInertiaTensor[i][j]-=blockMass*posArray[i]* posArray[j];
        for (int i = 0; i < 3; i++) localInertiaTensor[i][i] += blockMass * pos.lengthSqr();
    }*/
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
    double sq(double x)
    {
        return x*x;
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
        findSails(this.contraption);

        for (Map.Entry<BlockPos, BlockState> entry : sails.entrySet())
        {
            Vector3d pos = getLocalCoordinate(entry.getKey());
            Vector3d vel = getLocalVelocityAtPosition(pos);
            Vector3d normal = getFacingVector(entry.getValue());
            Vector3d force = normal.scale(-3.0f*normal.dot(vel));
            addForce(force,pos);
        }

        for (Map.Entry<UUID, SubcontraptionRigidbody> entry : subcontraptionRigidbodyMap.entrySet())
        {
            AbstractContraptionEntity entity = entry.getValue().entity;
            Contraption subContraption = entity.getContraption();

            entry.getValue().findSails(subContraption);

            if(entity instanceof ControlledContraptionEntity) {
                ControlledContraptionEntity controlledEntity = (ControlledContraptionEntity)entity;
                TileEntity te = entity.level.getBlockEntity(((ControlledContraptionEntityMixin)controlledEntity).getControllerPos());
                if (te instanceof PropellerBearingTileEntity) {
                    continue;
                }
            }

            for (Map.Entry<BlockPos, Template.BlockInfo> blockStateEntry : subContraption.getBlocks().entrySet())
            {
                if(blockStateEntry.getValue().state.getBlock() instanceof SailBlock) {

                    Vector3d pos = VecHelper.getCenterOf(blockStateEntry.getKey());
                    pos=entry.getValue().toParent(pos);

                    Vector3d vel = getLocalVelocityAtPosition(pos);
                    Vector3d normal = getFacingVector(blockStateEntry.getValue().state);
                    normal = entity.applyRotation(normal,1);
                    Vector3d force = normal.scale(-3.0f*normal.dot(vel));
                    addForce(force,pos);

                }
            }
        }

    }
    public Vector3d getPlotOffset()
    {
        BlockPos plotPos = AirshipManager.getPlotPosFromId(entity.plotId);
        if(entity.level.isClientSide) {
            return new Vector3d(0, plotPos.getY(), 0);
        } else {
            return new Vector3d(plotPos.getX(), plotPos.getY(), plotPos.getZ());
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
    public double getMass()
    {
        return mass;
    }

    public Vector3d getCenterOfMass() {
        return centerOfMass;
    }
    //angular momentum to angular velocity
    public Vector3d multiplyInertia(Vector3d v) {
        return inertiaTensorI.scale(v.x).add(inertiaTensorJ.scale(v.y)).add(inertiaTensorK.scale(v.z));
    }
    //angular velocity to angular momentum
    public Vector3d multiplyInertiaInverse(Vector3d v) {
        return inverseInertiaTensorI.scale(v.x).add(inverseInertiaTensorJ.scale(v.y)).add(inverseInertiaTensorK.scale(v.z));
    }

    public Vector3d rotate(Vector3d point) {
        return rotateQuat(point,orientation);
    }

    public Vector3d rotateInverse(Vector3d point) {
        return rotateQuatReverse(point,orientation);
    }

    public Vector3d rotateLocal(Vector3d point) {
        return rotateQuat(point,orientation);
    }

    public Vector3d rotateLocalInverse(Vector3d point) {
        return rotateQuatReverse(point,orientation);
    }

    public Vector3d toLocal(Vector3d globalPoint) {
        return rotateQuatReverse(globalPoint.subtract(entity.position()).subtract(centerOfMass),orientation).add(centerOfMass);
    }

    public Vector3d toGlobal(Vector3d localPoint) {
        return rotateQuat(localPoint.subtract(centerOfMass),orientation).add(centerOfMass).add(entity.position());
    }

    public Vector3d getVelocity() {
        return globalVelocity;
    }

    public Vector3d getVelocityAtPoint(Vector3d pos) {
        return globalVelocity.add(rotate(pos.cross(angularVelocity)));
    }

    public Vector3d getAngularVelocity() {
        return angularVelocity;
    }

    public void addForce(Vector3d force, Vector3d pos)
    {
        localForce = localForce.add(force);
        localTorque = localTorque.add(force.cross(pos));
    }
    public void addGlobalForce(Vector3d force, Vector3d pos)
    {
        globalForce = globalForce.add(force);
        globalTorque = globalTorque.add(force.cross(pos));
    }


    public void addVelocity(Vector3d pos, Vector3d velocity) {

    }


    public void addGlobalVelocity(Vector3d pos, Vector3d velocity) {

    }

    Vector3d getLocalVelocityAtPosition(Vector3d pos)
    {
        return localVelocity.add(pos.cross(angularVelocity));
    }
    public void applyImpulse(Vector3d pos, Vector3d impulse) {
        momentum = momentum.add(impulse);
        globalVelocity = momentum.scale(1.0 / getMass());

        // if(Math.abs(impulse.scale(1.0 / getMass()).lengthSqr()) < 0.05) return;

        Vector3d additionalAngularMomentum = rotateInverse(impulse).cross(pos);
        angularMomentum = angularMomentum.add(additionalAngularMomentum);
        updateRotation();
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

        Vector3d facingVector = getFacingVector(te.getBlockState());
        Vector3d direction= facingVector;
        // abs dir
        direction = new Vector3d(Math.abs(direction.x),Math.abs(direction.y),Math.abs(direction.z));
        float magnitude = 0.5f*te.getSpeed();

        Vector3d vector3d = entity.toGlobalVector(new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5,0.5,0.5), 1.0f);
        Vector3d pPos = vector3d;
        Vector3d veloVector = entity.applyRotation(facingVector,1.0f);

        float particleSpeed = te.getSpeed() / 256;
        veloVector = veloVector.scale(Math.abs(particleSpeed));
        if(Math.abs(particleSpeed) > 0) {
            entity.level.addParticle(new PropellerAirParticleData(new Vector3i(vector3d.x, vector3d.y, vector3d.z)), pPos.x, pPos.y, pPos.z, veloVector.x, veloVector.y, veloVector.z);
        }

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

            double referenceBuoyancy = PhysicsUtils.gravity*PhysicsUtils.getAirPressure(referencePos)*strengthScale;
            double referenceBuoyancyDerivative = PhysicsUtils.gravity* PhysicsUtils.getAirPressureDerivative(referencePos)*strengthScale;
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
