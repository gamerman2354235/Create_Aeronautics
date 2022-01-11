package com.eriksonn.createaeronautics.blocks.stationary_potato_cannon;

import com.eriksonn.createaeronautics.index.CABlockPartials;
import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.ITickableInstance;

import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.materials.IFlatLight;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public class StationaryPotatoCannonInstance extends ShaftInstance implements IDynamicInstance, ITickableInstance {

    final StationaryPotatoCannonTileEntity tile;
    final Direction facing;
    final float yRot;
    final float zRot;
    final float zRotPole;
    protected final OrientedData barrel;
    protected final OrientedData bellow1;
    protected final OrientedData bellow2;
    //protected final OrientedData pole;
    public StationaryPotatoCannonInstance(MaterialManager<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);
        this.tile = (StationaryPotatoCannonTileEntity)super.tile;
        this.facing = (Direction)this.blockState.getValue(DirectionalKineticBlock.FACING);
        boolean rotatePole = (Boolean)this.blockState.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE) ^ this.facing.getAxis() == Direction.Axis.Z;
        this.yRot = AngleHelper.horizontalAngle(this.facing);
        this.zRot = this.facing == Direction.UP ? 270.0F : (this.facing == Direction.DOWN ? 90.0F : 0.0F);
        this.zRotPole = rotatePole ? 90.0F : 0.0F;

       // PartialModel barrelModel = CABlockPartials.CANNON_BARREL;
       // InstanceMaterial M = this.getOrientedMaterial();
        //Instancer I = M.getModel(barrelModel,this.blockState);
        //this.barrel= (OrientedData)I.createInstance();

        this.barrel = (OrientedData)this.getOrientedMaterial().getModel(CABlockPartials.CANNON_BARREL, this.blockState).createInstance();
        this.bellow1 = (OrientedData)this.getOrientedMaterial().getModel(CABlockPartials.CANNON_BELLOW, this.blockState).createInstance();
        this.bellow2 = (OrientedData)this.getOrientedMaterial().getModel(CABlockPartials.CANNON_BELLOW, this.blockState).createInstance();
        //this.updateHandPose();
        this.relight(this.pos, new IFlatLight[]{this.barrel,this.bellow1,this.bellow2});
        //this.progress = this.getProgress(AnimationTickHolder.getPartialTicks());
        updateRotation(this.barrel,this.bellow1,this.bellow2, this.yRot, this.zRot, this.zRotPole);
        this.updatePosition();
    }
    public void tick() {

    }
    public void beginFrame() {
        this.updatePosition();
    }
    public void updateLight() {
        super.updateLight();
        this.relight(this.pos, new IFlatLight[]{this.barrel});
    }
    public void remove() {
        super.remove();
        this.barrel.delete();
        this.bellow1.delete();
        this.bellow2.delete();
       // this.pole.delete();
    }
    static void updateRotation(OrientedData barrel,OrientedData bellow1,OrientedData bellow2, float yRot, float zRot, float zRotPole) {

        Quaternion q = Direction.SOUTH.step().rotationDegrees(zRot);
        q.mul(Direction.UP.step().rotationDegrees(yRot));

        //hand.setRotation(q);

        q.mul(Direction.SOUTH.step().rotationDegrees(zRotPole));

        barrel.setRotation(q);
        bellow1.setRotation(q);
        q.mul(Direction.SOUTH.step().rotationDegrees(180));
        bellow2.setRotation(q);
    }

    float GetBarrelDistance(float BarrelTimer)
    {
        float x = BarrelTimer;
        return (float)(Math.E*x*Math.exp(-x));
    }
    private void updatePosition() {


        float normalizedTimer= (tile.BarrelTimer + AnimationTickHolder.getPartialTicks())/1;

        float distance = -0.15f*GetBarrelDistance(Math.max(normalizedTimer-0.5f,0));
        Vector3i facingVec = facing.getNormal();
        BlockPos blockPos = getInstancePosition();
        Vector3f BasePos = new Vector3f(blockPos.getX(),blockPos.getY(),blockPos.getZ());
        Vector3f OffsetPos = new Vector3f(facingVec.getX(),facingVec.getY(),facingVec.getZ());

        barrel.setPosition(VectorAdd(BasePos,VectorScale(OffsetPos,distance)));
        if(tile.state== StationaryPotatoCannonTileEntity.State.FIRING)
            distance=normalizedTimer;
        else
            distance = 1f-Math.min(tile.chargeProgress+tile.getChargeUpSpeed()*AnimationTickHolder.getPartialTicks(),1);
        distance=Math.min(distance,1);
        distance=Math.max(distance,0);
        distance*=0.15;


        Quaternion q = Direction.SOUTH.step().rotationDegrees(zRot);
        q.mul(Direction.UP.step().rotationDegrees(yRot));

        q.mul(Direction.SOUTH.step().rotationDegrees(zRotPole));

        Quaternion A = new Quaternion(0,1,0,0);
        Quaternion q2 = q.copy();
        q.conj();
        q2.mul(A);
        q2.mul(q);
        OffsetPos=new Vector3f(q2.i(),q2.j(),q2.k());

        bellow1.setPosition(VectorAdd(BasePos,VectorScale(OffsetPos,-distance)));
        bellow2.setPosition(VectorAdd(BasePos,VectorScale(OffsetPos,distance)));
    }
    Vector3f VectorAdd(Vector3f A, Vector3f B)
    {
        return new Vector3f(A.x()+B.x(),A.y()+B.y(),A.z()+B.z());
    }
    Vector3f VectorScale(Vector3f A, float M)
    {
        return new Vector3f(A.x()*M,A.y()*M,A.z()*M);
    }
}
