package com.eriksonn.createaeronautics.contraptions;

import com.eriksonn.createaeronautics.blocks.propeller_bearing.PropellerBearingTileEntity;
import com.eriksonn.createaeronautics.index.CATileEntities;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.model.pipeline.BlockInfo;

import java.util.Map;
import java.util.Vector;


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
    Vector3d totalForce=Vector3d.ZERO;
    Vector3d tourqe=Vector3d.ZERO;

    final double dt=0.05f;//converts the time unit to seconds instead of ticks

    public PhysicsManager(AirshipContraptionEntity entity)
    {
        orientation=Quaternion.ONE.copy();
        contraption=entity.airshipContraption;
        this.entity=entity;
        momentum=Vector3d.ZERO;
    }
    public void tick()
    {

        contraption=entity.airshipContraption;
        if(contraption==null)
            return;
        updateInertia();
        updateTileEntityInteractions();
        updateRotation();
        momentum=momentum.add(totalForce);
        totalForce=Vector3d.ZERO;

        momentum=momentum.scale(0.995);
        Vector3d velocity=rotateQuat(momentum.scale(dt/mass),orientation);
        entity.quat=orientation.copy();
        entity.velocity=velocity.scale(dt);
        entity.setDeltaMovement(velocity.scale(dt));
        entity.move(velocity.x*dt,velocity.y*dt,velocity.z*dt);
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
        //TODO: implement spectral decomposition here that does not mess up for symmetric setups,
        // and that keeps the orientation as close as possible to the original

        principialInertia[0]=localInertiaTensor[0][0];
        principialInertia[1]=localInertiaTensor[1][1];
        principialInertia[2]=localInertiaTensor[2][2];
        principialRotation=Quaternion.ONE.copy();

        tourqe = new Vector3d(0,1,0).scale(tourqe.y);
        angularMomentum=angularMomentum.add(tourqe.scale(dt));
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
        angularMomentum=angularMomentum.scale(0.99);
        Vector3d v = angularVelocity.scale(dt*0.5f);
        Quaternion q = new Quaternion((float)v.x,(float)v.y,(float)v.z, 1.0f);
        orientation.mul(q);
        orientation.normalize();

        tourqe=Vector3d.ZERO;
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
        totalForce = totalForce.add(force);
        tourqe = tourqe.add(force.cross(pos));
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
}
