package com.example.examplemod.contraptions;

import com.example.examplemod.contraptions.rendering.AirshipRenderer;
import com.example.examplemod.contraptions.rendering.CGlobal;
import com.example.examplemod.dimension.AirshipDimensionManager;
import com.example.examplemod.index.CADimensions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
//import com.qouteall.immersive_portals.CHelper;
import com.qouteall.immersive_portals.ClientWorldLoader;
import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.OFInterface;
import com.qouteall.immersive_portals.Global;
import com.qouteall.immersive_portals.SodiumInterface;
import com.qouteall.immersive_portals.block_manipulation.BlockManipulationClient;
import com.qouteall.immersive_portals.ducks.*;
import com.qouteall.immersive_portals.render.context_management.*;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;
import java.util.function.Consumer;

public class AirshipContraptionRenderer extends EntityRenderer<AirshipContraptionEntity> {
    AirshipRenderer renderer=new AirshipRenderer();
        public AirshipContraptionRenderer(EntityRendererManager manager) {
            super(manager);
        }
        public ResourceLocation getTextureLocation(AirshipContraptionEntity p_110775_1_) {
            return null;
        }

        public boolean shouldRender(AirshipContraptionEntity entity, ClippingHelper clippingHelper, double cameraX, double cameraY, double cameraZ) {
            if (entity.getContraption() == null) {
                return false;
            } else {
                return !entity.isAlive() ? false : super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
            }
        }
    public void render(AirshipContraptionEntity entity, float yaw, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers, int overlay) {
        super.render(entity, yaw, partialTicks, ms, buffers, overlay);
        ContraptionMatrices matrices = new ContraptionMatrices(ms, entity);
        Contraption contraption = entity.getContraption();
        if (contraption != null) {
            //ContraptionRenderDispatcher.render(entity, contraption, matrices, buffers);
        }


        ClientWorld w= ClientWorldLoader.getWorld(AirshipDimensionManager.INSTANCE.getWorld().dimension());

AirshipContraption aircon=(AirshipContraption)contraption;
Vector3i vec1 = new Vector3i((float)entity.position().x-5,(float)entity.position().y-5,(float)entity.position().z-5);
Vector3i vec2 = new Vector3i((float)entity.position().x+5,(float)entity.position().y+5,(float)entity.position().z+5);

        vec1=new Vector3i(-1,-1,-1);
        vec2=new Vector3i(1,1,1);

        renderer.setActive(true);
        renderer.tick();
        renderer.update();

        renderer.display(w,aircon.anchor,new MutableBoundingBox(vec1.getX(),vec1.getY(),vec1.getZ(),vec2.getX(),vec2.getY(),vec2.getZ()));
        renderer.render(ms,new SuperRenderTypeBuffer());
        //if(aircon!=null) {
            //if(aircon.storageWorld !=null) {
                //PortalRendering.onBeginPortalWorldRendering();
                int renderDistance = 3;

                //WorldRenderInfo worldRenderInfo =new WorldRenderInfo(w, CGlobal.originalCamera.getPosition(), new Matrix4f(new Quaternion(0,0,0,1)), false, entity.getUUID(), renderDistance);
                //WorldRenderInfo.pushRenderInfo(worldRenderInfo);

        //Test(w,Runnable::run);
                //MyGameRenderer.renderWorldNew(worldRenderInfo, Runnable::run);
                //this.switchAndRenderTheWorld(w,CGlobal.originalCamera.getPosition(),CGlobal.originalCamera.getPosition(),Runnable::run,3);

                //WorldRenderInfo.popRenderInfo();
                //PortalRendering.onEndPortalWorldRendering();
                //GlStateManager._enableDepthTest();
                //GlStateManager._disableBlend();
                //MyRenderHelper.restoreViewPort();
            //}
        //}
    }
    public static Minecraft client = Minecraft.getInstance();
        private static void Test(ClientWorld newWorld,Consumer<Runnable> invokeWrapper)
        {
            float tickDelta = RenderStates.tickDelta;
            ClientWorld oldEntityWorld = (ClientWorld)client.cameraEntity.level;
            WorldRenderer oldWorldRenderer = client.levelRenderer;
            RegistryKey<World> newDimension = newWorld.dimension();
            WorldRenderer worldRenderer = ClientWorldLoader.getWorldRenderer(newDimension);
            ((IEMinecraftClient)client).setWorldRenderer(worldRenderer);
            client.level = newWorld;
            invokeWrapper.accept(() -> {
                client.gameRenderer.renderLevel(tickDelta, Util.getNanos(), new MatrixStack());
            });
            client.level = oldEntityWorld;
            ((IEMinecraftClient)client).setWorldRenderer(oldWorldRenderer);
        }
    private static void switchAndRenderTheWorld(ClientWorld newWorld, Vector3d thisTickCameraPos, Vector3d lastTickCameraPos, Consumer<Runnable> invokeWrapper, int renderDistance) {
        resetGlStates();
        Entity cameraEntity = client.cameraEntity;
        Vector3d oldEyePos = McHelper.getEyePos(cameraEntity);
        Vector3d oldLastTickEyePos = McHelper.getLastTickEyePos(cameraEntity);
        RegistryKey<World> oldEntityDimension = cameraEntity.level.dimension();
        ClientWorld oldEntityWorld = (ClientWorld)cameraEntity.level;
        RegistryKey<World> newDimension = newWorld.dimension();
        McHelper.setEyePos(cameraEntity, thisTickCameraPos, lastTickCameraPos);
        cameraEntity.level = newWorld;
        WorldRenderer worldRenderer = ClientWorldLoader.getWorldRenderer(newDimension);
        //CHelper.checkGlError();
        float tickDelta = RenderStates.tickDelta;
        if (com.qouteall.immersive_portals.CGlobal.useHackedChunkRenderDispatcher) {
            ((IEWorldRenderer)worldRenderer).getBuiltChunkStorage().repositionCamera(cameraEntity.getX(), cameraEntity.getZ());
        }

        IEGameRenderer ieGameRenderer = (IEGameRenderer)client.gameRenderer;
        DimensionRenderHelper helper = ClientWorldLoader.getDimensionRenderHelper(RenderDimensionRedirect.getRedirectedDimension(newDimension));
        NetworkPlayerInfo playerListEntry = Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId());
        ActiveRenderInfo newCamera = new ActiveRenderInfo();
        WorldRenderer oldWorldRenderer = client.levelRenderer;
        LightTexture oldLightmap = client.gameRenderer.lightTexture();
        GameType oldGameMode = playerListEntry.getGameMode();
        boolean oldNoClip = client.player.noPhysics;
        boolean oldDoRenderHand = ieGameRenderer.getDoRenderHand();
        OFInterface.createNewRenderInfosNormal.accept(worldRenderer);
        ObjectList oldVisibleChunks = ((IEWorldRenderer)oldWorldRenderer).getVisibleChunks();
        RayTraceResult oldCrosshairTarget = client.hitResult;
        ActiveRenderInfo oldCamera = client.gameRenderer.getMainCamera();
        ShaderGroup oldTransparencyShader = ((IEWorldRenderer)oldWorldRenderer).portal_getTransparencyShader();
        ShaderGroup newTransparencyShader = ((IEWorldRenderer)worldRenderer).portal_getTransparencyShader();
        RenderTypeBuffers oldBufferBuilder = ((IEWorldRenderer)worldRenderer).getBufferBuilderStorage();
        RenderTypeBuffers oldClientBufferBuilder = client.renderBuffers();
        boolean oldChunkCullingEnabled = client.smartCull;
        ((IEWorldRenderer)oldWorldRenderer).setVisibleChunks(new ObjectArrayList());
        int oldRenderDistance = ((IEWorldRenderer)worldRenderer).portal_getRenderDistance();
        ((IEMinecraftClient)client).setWorldRenderer(worldRenderer);
        client.level = newWorld;
        ieGameRenderer.setLightmapTextureManager(helper.lightmapTexture);
        TileEntityRendererDispatcher.instance.level = newWorld;
        ((IEPlayerListEntry)playerListEntry).setGameMode(GameType.SPECTATOR);
        client.player.noPhysics = true;
        ieGameRenderer.setDoRenderHand(false);
        GlStateManager._matrixMode(5888);
        GlStateManager._pushMatrix();
        //FogRendererContext.swappingManager.pushSwapping(RenderDimensionRedirect.getRedirectedDimension(newDimension));
        ((IEParticleManager)client.particleEngine).mySetWorld(newWorld);
        if (BlockManipulationClient.remotePointedDim == newDimension) {
            client.hitResult = BlockManipulationClient.remoteHitResult;
        }

        ieGameRenderer.setCamera(newCamera);
        if (Global.useSecondaryEntityVertexConsumer) {
            //((IEWorldRenderer)worldRenderer).setBufferBuilderStorage(secondaryBufferBuilderStorage);
            //((IEMinecraftClient)client).setBufferBuilderStorage(secondaryBufferBuilderStorage);
        }

        //Object newSodiumContext = SodiumInterface.createNewRenderingContext.apply(worldRenderer);
        //Object oldSodiumContext = SodiumInterface.switchRenderingContext.apply(worldRenderer, newSodiumContext);
        //((IEWorldRenderer)oldWorldRenderer).portal_setTransparencyShader((ShaderGroup)null);
        //((IEWorldRenderer)worldRenderer).portal_setTransparencyShader((ShaderGroup)null);
        //((IEWorldRenderer)worldRenderer).portal_setRenderDistance(renderDistance);
        if (Global.looseVisibleChunkIteration) {
            client.smartCull = false;
        }

        if (!RenderStates.isDimensionRendered(newDimension)) {
            helper.lightmapTexture.updateLightTexture(0.0F);
        }

        try {
            invokeWrapper.accept(() -> {
                ///client.getProfiler().push("render_portal_content");
                client.gameRenderer.renderLevel(tickDelta, Util.getNanos(), new MatrixStack());
                //client.getProfiler().pop();
            });
        } catch (Throwable var34) {
            //limitedLogger.invoke(var34::printStackTrace);
        }

        //SodiumInterface.switchRenderingContext.apply(worldRenderer, oldSodiumContext);
        ((IEMinecraftClient)client).setWorldRenderer(oldWorldRenderer);
        client.level = oldEntityWorld;
        ieGameRenderer.setLightmapTextureManager(oldLightmap);
        TileEntityRendererDispatcher.instance.level = oldEntityWorld;
        ((IEPlayerListEntry)playerListEntry).setGameMode(oldGameMode);
        client.player.noPhysics = oldNoClip;
        ieGameRenderer.setDoRenderHand(oldDoRenderHand);
        GlStateManager._matrixMode(5888);
        GlStateManager._popMatrix();
        ((IEParticleManager)client.particleEngine).mySetWorld(oldEntityWorld);
        client.hitResult = oldCrosshairTarget;
        ieGameRenderer.setCamera(oldCamera);
        //((IEWorldRenderer)oldWorldRenderer).portal_setTransparencyShader(oldTransparencyShader);
        //((IEWorldRenderer)worldRenderer).portal_setTransparencyShader(newTransparencyShader);
        FogRendererContext.swappingManager.popSwapping();
        ((IEWorldRenderer)oldWorldRenderer).setVisibleChunks(oldVisibleChunks);
        ((IEWorldRenderer)worldRenderer).setBufferBuilderStorage(oldBufferBuilder);
        ((IEMinecraftClient)client).setBufferBuilderStorage(oldClientBufferBuilder);
        ((IEWorldRenderer)worldRenderer).portal_setRenderDistance(oldRenderDistance);
        if (Global.looseVisibleChunkIteration) {
            client.smartCull = oldChunkCullingEnabled;
        }

        client.getEntityRenderDispatcher().prepare(client.level, oldCamera, client.crosshairPickEntity);
        //CHelper.checkGlError();
        cameraEntity.level = oldEntityWorld;
        McHelper.setEyePos(cameraEntity, oldEyePos, oldLastTickEyePos);
        resetGlStates();
    }
    public static void resetGlStates() {
        GlStateManager._disableAlphaTest();
        GlStateManager._enableCull();
        GlStateManager._disableBlend();
        RenderHelper.turnOff();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        client.gameRenderer.overlayTexture().teardownOverlayColor();
    }

    }

