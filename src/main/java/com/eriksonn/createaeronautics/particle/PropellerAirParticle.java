package com.eriksonn.createaeronautics.particle;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.fan.IAirCurrentSource;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.content.contraptions.particle.AirFlowParticleData;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

public class PropellerAirParticle extends SimpleAnimatedParticle {
    Vector3d motion;
    protected PropellerAirParticle(ClientWorld world, double x, double y, double z, double dx, double dy,
                          double dz, IAnimatedSprite sprite) {
        super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
        this.quadSize *= 0.75F;
        this.lifetime = 20;
        hasPhysics = false;
        selectSprite(7);
        Vector3d offset = VecHelper.offsetRandomly(Vector3d.ZERO, Create.RANDOM, .5f);
        this.setPos(x + offset.x, y + offset.y, z + offset.z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        motion=new Vector3d(dx,dy,dz);
        setAlpha(.25f);
    }
    @Nonnull
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    private void dissipate() {
        remove();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }else
        {
            selectSprite((int) MathHelper.clamp((this.age / (float)this.lifetime) * 8 + level.random.nextInt(1), 0, 7));
            xd = motion.x;
            yd = motion.y;
            zd = motion.z;
            double friction = 0.2*motion.lengthSqr();
            friction=Math.min(friction,0.5f);
            motion=motion.scale(1.0-friction);
            this.move(this.xd, this.yd, this.zd);
        }

    }

    public int getLightColor(float partialTick) {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        return this.level.isLoaded(blockpos) ? WorldRenderer.getLightColor(level, blockpos) : 0;
    }

    private void selectSprite(int index) {
        setSprite(sprites.get(index, 8));
    }

    public static class Factory implements IParticleFactory<PropellerAirParticleData> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(PropellerAirParticleData data,ClientWorld worldIn, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new PropellerAirParticle(worldIn, x, y, z,xSpeed,ySpeed,zSpeed, this.spriteSet);
        }
    }
}
