package com.eriksonn.createaeronautics.contraptions.rendering;

import com.eriksonn.createaeronautics.dimension.AirshipWorld;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
//import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.*;

public class AirshipRenderer {
    private final Map<RenderType, SuperByteBuffer> bufferCache = new HashMap(getLayerCount());
    private final Set<RenderType> usedBlockRenderLayers = new HashSet(getLayerCount());
    private final Set<RenderType> startedBufferBuilders = new HashSet(getLayerCount());
    private boolean active;
    private boolean changed = false;
    protected AirshipWorld schematic;
    private BlockPos anchor;
    MutableBoundingBox boundingBox;

    public AirshipRenderer() {
    }

    public void display(AirshipWorld world,BlockPos anchor,MutableBoundingBox boundingBox) {
        this.boundingBox=boundingBox;
        this.anchor = anchor;
        this.schematic = world;
        this.active = true;
        this.changed = true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void update() {
        this.changed = true;
    }

    public void tick() {
        if (this.active) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null && this.changed) {
                this.redraw(mc);
                this.changed = false;
            }
        }
    }

    public void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
        if (this.active) {
            buffer.getBuffer(RenderType.solid());
            Iterator var3 = RenderType.chunkBufferLayers().iterator();

            while(var3.hasNext()) {
                RenderType layer = (RenderType)var3.next();
                if (this.usedBlockRenderLayers.contains(layer)) {
                    SuperByteBuffer superByteBuffer = (SuperByteBuffer)this.bufferCache.get(layer);
                    superByteBuffer.renderInto(ms, buffer.getBuffer(layer));
                }
            }

            //TileEntityRenderHelper.renderTileEntities(this.schematic, this.schematic.getRenderedTileEntities(), ms, buffer);
        }
    }

    protected void redraw(Minecraft minecraft) {
        this.usedBlockRenderLayers.clear();
        this.startedBufferBuilders.clear();
        World blockAccess = this.schematic;
        BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRenderer();
        List<BlockState> blockstates = new LinkedList();
        Map<RenderType, BufferBuilder> buffers = new HashMap();
        MatrixStack ms = new MatrixStack();
        BlockPos.betweenClosedStream(boundingBox).forEach((localPos) -> {
            ms.pushPose();
            MatrixTransformStack.of(ms).translate(localPos);
            BlockPos pos = localPos.offset(this.anchor);
            BlockState state = blockAccess.getBlockState(pos);
            Iterator var10 = RenderType.chunkBufferLayers().iterator();

            while(var10.hasNext()) {
                RenderType blockRenderLayer = (RenderType)var10.next();
                if (RenderTypeLookup.canRenderInLayer(state, blockRenderLayer)) {
                    ForgeHooksClient.setRenderLayer(blockRenderLayer);
                    if (!buffers.containsKey(blockRenderLayer)) {
                        buffers.put(blockRenderLayer, new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize()));
                    }

                    BufferBuilder bufferBuilder = (BufferBuilder)buffers.get(blockRenderLayer);
                    if (this.startedBufferBuilders.add(blockRenderLayer)) {
                        bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
                    }

                    TileEntity tileEntity = blockAccess.getBlockEntity(localPos);
                    if (blockRendererDispatcher.renderModel(state, pos, blockAccess, ms, bufferBuilder, true, minecraft.level.random, (IModelData)(tileEntity != null ? tileEntity.getModelData() : EmptyModelData.INSTANCE))) {
                        this.usedBlockRenderLayers.add(blockRenderLayer);
                    }

                    blockstates.add(state);
                }
            }

            ForgeHooksClient.setRenderLayer((RenderType)null);
            ms.popPose();
        });
        Iterator var7 = RenderType.chunkBufferLayers().iterator();

        while(var7.hasNext()) {
            RenderType layer = (RenderType)var7.next();
            if (this.startedBufferBuilders.contains(layer)) {
                BufferBuilder buf = (BufferBuilder)buffers.get(layer);
                buf.end();
                this.bufferCache.put(layer, new SuperByteBuffer(buf));
            }
        }

    }

    private static int getLayerCount() {
        return RenderType.chunkBufferLayers().size();
    }
}
