package com.eriksonn.createaeronautics.physics;

import com.eriksonn.createaeronautics.physics.collision.shape.ICollisionShape;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractContraptionRigidbody implements IRigidbody{

    public Vector3d localCenterOfMass=Vector3d.ZERO;
    public double[][] localInertiaTensor=new double[3][3];
    public double localMass;
    public Map<BlockPos, BlockState> sails = new HashMap<>();
    public void generateMassDependentParameters(Contraption contraption,Vector3d offset)
    {
        localMass=0;
        localCenterOfMass=Vector3d.ZERO;
        localInertiaTensor=new double[3][3];
        for (Map.Entry<BlockPos, Template.BlockInfo> entry : contraption.getBlocks().entrySet()) {
            if (!entry.getValue().state.isAir()) {
                double blockMass=PhysicsUtils.getBlockMass(entry.getValue());
                Vector3d pos=new Vector3d(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ());

                localCenterOfMass=localCenterOfMass.add(pos.scale(blockMass));
                localMass+=blockMass;
            }
        }


        localCenterOfMass=localCenterOfMass.scale(1.0/localMass);
        for (Map.Entry<BlockPos, Template.BlockInfo> entry : contraption.getBlocks().entrySet()) {
            if (!entry.getValue().state.isAir()) {
                double blockMass=PhysicsUtils.getBlockMass(entry.getValue());
                Vector3d pos=new Vector3d(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ());
                pos=pos.subtract(offset);
                pos=pos.subtract(localCenterOfMass);

                double[] posArray=new double[]{pos.x,pos.y,pos.z};
                //TODO: Fix single blocks or lines of blocks causing zero inertia, as that leads to nan issues later on
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        localInertiaTensor[i][j]-=blockMass*posArray[i]* posArray[j];
                for (int i = 0; i < 3; i++) localInertiaTensor[i][i] += blockMass * pos.lengthSqr();
            }
        }
    }
    public void findSails(Contraption contraption)
    {
        sails.clear();
        for (Map.Entry<BlockPos, Template.BlockInfo> entry : contraption.getBlocks().entrySet()) {
            if(entry.getValue().state.getBlock() instanceof SailBlock) {
                sails.put(entry.getKey(),entry.getValue().state);
            }
        }
    }
    public double getLocalMass() { return localMass; }
    public Vector3d getLocalCenterOfMass(){ return localCenterOfMass; }

    //#region Collision
    HashMap<BlockPos, List<ICollisionShape>> collisionShapes = new HashMap<>();

    public HashMap<BlockPos, List<ICollisionShape>> getCollisionShapes() {
        return collisionShapes;
    }

    public void setCollisionShapes(HashMap<BlockPos, List<ICollisionShape>> collisionShapes) {
        this.collisionShapes = collisionShapes;
    }
    //#endregion

    public abstract AbstractContraptionEntity getContraption();
    abstract public Vector3d getPlotOffset();
}
