package com.eriksonn.createaeronautics.inspect;

import com.eriksonn.createaeronautics.contraptions.AirshipContraption;
import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.eriksonn.createaeronautics.index.CABlocks;
import com.eriksonn.createaeronautics.network.NetworkMain;
import com.eriksonn.createaeronautics.network.packet.PausePhysicsPacket;
import com.eriksonn.createaeronautics.particle.PropellerAirParticle;
import com.eriksonn.createaeronautics.particle.PropellerAirParticleData;
import com.eriksonn.createaeronautics.physics.SimulatedContraptionRigidbody;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.ponder.*;
import com.simibubi.create.foundation.ponder.content.PonderTag;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.*;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.feature.template.Template;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static com.eriksonn.createaeronautics.inspect.PhysicsScenario.WIND_TUNNEL;

public class InspectUI extends NavigatableSimiScreen {


    public static NavigatableSimiScreen of(AirshipContraptionEntity movedContraption) {
        return new InspectUI(movedContraption, PhysicsScenario.NONE);
    }

    public static NavigatableSimiScreen of(AirshipContraptionEntity movedContraption, PhysicsScenario scenario) {
        return new InspectUI(movedContraption, scenario);
    }

    private InspectUI(AirshipContraptionEntity movedContraption, PhysicsScenario scenario) {
        this.airship = movedContraption;
        this.scenario = scenario;
    }

    public static float getPartialTicks() {
        float renderPartialTicks = Minecraft.getInstance()
                .getFrameTime();

        return renderPartialTicks;
    }

    AirshipContraptionEntity airship;

    PhysicsScenario scenario = PhysicsScenario.NONE;
    SimulatedContraptionRigidbody rigidbody;
    int basePlateTop = 0;


    // ======== WIDGETS ========

    private PonderButton close, pause, toggleCOM;
    PonderButton playPauseScenario;
    PonderButton rotateScenario;
    QuatWorldSection section;
    WorldSectionElement propeller;

    // ==========================

    // ========= SCENARIO ========

    int scenarioStatementWidth = 130;
    int scenarioStatementStartX;
    int scenarioStatementEndX;
    int scenarioStatementY;

    boolean scenarioHasBegunBefore = false;
    boolean playScenario = false;

    BlockPos propellerCenter;
    int propellerLength = 9;

    Vector3d originalPosition;
    Vector3d sectionPosition;
    Quaternion sectionRotation = Quaternion.ONE;

    public boolean inScenario() {
        return scenario != PhysicsScenario.NONE;
    }

    // ==========================

    PonderScene scene;

    @Override
    protected void init() {
        widgets.clear();
        super.init();

        // ======== Constants ========
        scenarioStatementWidth = 230;
        scenarioStatementY = 40;
        scenarioStatementStartX = (width - scenarioStatementWidth) / 2;
        scenarioStatementEndX = scenarioStatementStartX + scenarioStatementWidth;
        int spacing = 60;
        int simulateWidth = 20;

        GameSettings bindings = minecraft.options;

        if(!inScenario()) {
            widgets.add(new PonderButton(width / 2 - 20 + spacing, height - 40)
                    .withShortcut(bindings.keyInventory)
                    .showing(AllIcons.I_MTD_CLOSE)
                    .withCallback(this::onClose));

            widgets.add(pause = new PonderButton(width / 2 - 20, height - 40)
                    .withShortcut(bindings.keyDrop)
                    .showing(airship.playPhysics ? AllIcons.I_PAUSE : AllIcons.I_PLAY)
                    .withCallback(() -> {
                        if (airship.playPhysics) {
                            airship.playPhysics = false;
                            pause.showing(AllIcons.I_PLAY);
                        } else {
                            airship.playPhysics = true;
                            pause.showing(AllIcons.I_PAUSE);
                        }
                        NetworkMain.sendToServer(new PausePhysicsPacket(airship.plotId, !airship.playPhysics));
                    }));

            widgets.add(new PonderButton(width / 2 - 20 - spacing, height - 40)
                    .showing(AllIcons.I_TRASH)
                    .withCallback(this::onClose));
        }

        // simulate button
        widgets.add(new PonderButton(width - 20 - simulateWidth, 20).showing(AllIcons.I_PLACEMENT_SETTINGS).withCallback(this::onSimulateOpen));

        // simulation widgets
        if(inScenario()) {
            widgets.add(playPauseScenario = new PonderButton(scenarioStatementEndX - 20 - 8, scenarioStatementY + 6).showing(AllIcons.I_PLAY).withCallback(() -> {
                playScenario = !playScenario;

                if(!scenarioHasBegunBefore) {
                    scenarioHasBegunBefore = true;
                    rotateScenario.visible = false;
                }

                if(playScenario) {
                    playPauseScenario.showing(AllIcons.I_PAUSE);
                } else {
                    playPauseScenario.showing(AllIcons.I_PLAY);
                }
            }));
            widgets.add(rotateScenario = new PonderButton(scenarioStatementEndX - 20 - 20 - 8 - 8, scenarioStatementY + 6).showing(AllIcons.I_ROTATE_CCW).withCallback(() -> {
                rigidbody.orientation.mul(new Quaternion(new Vector3f(0, 1, 0), (float) Math.PI / 2, false));
            }));
        }

        PonderButton comButton = new PonderButton(21, 21 + 50/*, 14, 14*/);
        widgets.add(comButton.showing(AllIcons.I_CONFIRM));

        scene = new PonderScene(new PonderWorld(BlockPos.ZERO, Minecraft.getInstance().level), "", new ResourceLocation(""), new ArrayList<PonderTag>());

        scene.getWorld().scene = scene;

        Map<BlockPos, Template.BlockInfo> blocks = airship.getContraption().getBlocks();

        int minx = 1000, miny = 1000, minz = 1000, maxx = -1000, maxy = -100, maxz = -1000;

        for (Map.Entry<BlockPos, Template.BlockInfo> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey().offset(0, 128, 0);
            scene.getWorld().setBlock(pos, entry.getValue().state, 0);
            minx = Math.min(minx, pos.getX());
            miny = Math.min(miny, pos.getY());
            minz = Math.min(minz, pos.getZ());
            maxx = Math.max(maxx, pos.getX());
            maxy = Math.max(maxy, pos.getY());
            maxz = Math.max(maxz, pos.getZ());
        }

        basePlateTop = miny - 2;

        rigidbody = new SimulatedContraptionRigidbody((AirshipContraption) airship.getContraption(), new PonderPhysicsAdapter(this));

        section = new QuatWorldSection(rigidbody, Selection.of(new MutableBoundingBox(minx, miny, minz, maxx, maxy, maxz)));
        section.forceApplyFade(1.0f);
        section.setCenterOfRotation(airship.centerOfMassOffset.add(0.5, 128.5, 0.5));

        scene.addElement(section);
        scene.getWorld().pushFakeLight(15);

        scene.setFinished(false);


        widgets.add(backTrack = new PonderButton(31, height - 31 - 20).enableFade(0, 5)
                .showing(AllIcons.I_MTD_CLOSE)
                .withCallback(() -> ScreenOpener.openPreviousScreen(this, Optional.empty())));
        backTrack.fade(1);

        originalPosition = new Vector3d(0, 128, 0).add(airship.centerOfMassOffset);
        sectionPosition = originalPosition;

        if(inScenario()) {

            originalPosition = originalPosition.add(airship.centerOfMassOffset);

            if(scenario == PhysicsScenario.BALANCE) {

                int fluidTop = (miny) - 3;
                int fluidWidth = maxx - minx + 2;
                int fluidDepth = maxz - minz + 2;
                int fluidBottom = (miny) - 4;

                // fill blocks
                for(int x = minx - 4; x <= maxx + 4; x++) {
                    for(int y = fluidBottom; y < fluidTop; y++) {
                        for(int z = minz - 4; z <= maxz + 4 ; z++) {
                            BlockPos pos = new BlockPos(x, y, z);

                            // dither checkerboard on x/z axis
                            if((x % 2 == 0 && z % 2 != 0) || (x % 2 != 0 && z % 2 == 0)) {
                                scene.getWorld().setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 0);
                            } else {
                                scene.getWorld().setBlock(pos, Blocks.SNOW_BLOCK.defaultBlockState(), 0);
                            }

                        }
                    }
                }

                WorldSectionElement fluidSection = new WorldSectionElement(Selection.of(new MutableBoundingBox(
                        -fluidWidth / 2,
                        fluidBottom,
                        -fluidWidth / 2,
                        fluidWidth / 2,
                        fluidTop,
                        fluidWidth / 2
                )));
                fluidSection.forceApplyFade(1.0f);

                scene.addElement(fluidSection);

            }
            if(scenario == WIND_TUNNEL) {

                rigidbody.doCollisions = false;
                int maxX = maxx + 10;
                int midY = (miny + maxy) / 2;

                // propeller center
                propellerCenter = new BlockPos(maxX, midY, 0);

                // propeller
                scene.getWorld().setBlock(propellerCenter, AllBlocks.ANDESITE_CASING.getDefaultState(), 0);


                // propeller blades
                for(int i = 1; i < propellerLength; i++) {
                    BlockPos up = propellerCenter.offset(0, i, 0);
                    BlockPos down = propellerCenter.offset(0, -i, 0);
                    BlockPos left = propellerCenter.offset(0, 0, -i);
                    BlockPos right = propellerCenter.offset(0, 0, i);
                    scene.getWorld().setBlock(up, AllBlocks.SAIL.getDefaultState().setValue(SailBlock.FACING, Direction.WEST), 0);
                    scene.getWorld().setBlock(down, AllBlocks.SAIL.getDefaultState().setValue(SailBlock.FACING, Direction.WEST), 0);
                    scene.getWorld().setBlock(left, AllBlocks.SAIL.getDefaultState().setValue(SailBlock.FACING, Direction.WEST), 0);
                    scene.getWorld().setBlock(right, AllBlocks.SAIL.getDefaultState().setValue(SailBlock.FACING, Direction.WEST), 0);
                }

                // section
                propeller = new WorldSectionElement(Selection.of(new MutableBoundingBox(
                        maxX - 1,
                        midY - propellerLength,
                        -propellerLength,
                        maxX + 1,
                        midY + propellerLength,
                        propellerLength
                )));

                propeller.forceApplyFade(1.0f);

                scene.addElement(propeller);
            }
        }

    }

    private void onSimulateOpen() {
        ScreenOpener.transitionTo(InspectScenariosScreen.of(airship));
    }

    public Vector3d toXYZ(Quaternion quat) {
        float f = quat.r() * quat.r();
        float f1 = quat.i() * quat.i();
        float f2 = quat.j() * quat.j();
        float f3 = quat.k() * quat.k();
        float f4 = f + f1 + f2 + f3;
        float f5 = 2.0F * quat.r() * quat.i() - 2.0F * quat.j() * quat.k();
        float f6 = (float)Math.asin((double)(f5 / f4));
        return Math.abs(f5) > 0.999F * f4 ? new Vector3d(2.0F * (float)Math.atan2((double)quat.i(), (double)quat.r()), f6, 0.0F) : new Vector3d((float)Math.atan2((double)(2.0F * quat.j() * quat.k() + 2.0F * quat.i() * quat.r()), (double)(f - f1 - f2 + f3)), f6, (float)Math.atan2((double)(2.0F * quat.i() * quat.j() + 2.0F * quat.r() * quat.k()), (double)(f + f1 - f2 - f3)));
    }

    public Vector3d toXYZDegrees(Quaternion quat) {
        Vector3d vector3f = this.toXYZ(quat);
        return new Vector3d((float)Math.toDegrees((double)vector3f.x()), (float)Math.toDegrees((double)vector3f.y()), (float)Math.toDegrees((double)vector3f.z()));
    }



    @Override
    public void tick() {
        super.tick();
        if(scene == null) return;
        scene.tick();

        if(inScenario()) {
            if(playScenario) {
                rigidbody.tick();

                // ========= Wind Tunnel Particles =========
                if(scenario == WIND_TUNNEL) {

                    rigidbody.momentum = new Vector3d(10, 0, 0).scale(rigidbody.getMass());
                    rigidbody.doGravity = false;

                    propeller.setAnimatedRotation(propeller.getAnimatedRotation().add(16.0, 0.0, 0.0), false);


                    for(int i = 0; i < 10; i++) {

                        Vector3d pos = new Vector3d(propellerCenter.getX(), propellerCenter.getY(), propellerCenter.getZ());

                        // random rotation
                        pos = pos.add(new Vector3d(
                                0.0,
                                (Math.random() - 0.5) * 0.5,
                                (Math.random() - 0.5) * 0.5
                        ).scale(propellerLength * 2.0));

                        // spawn particles
                        scene.getWorld().addParticle(new PropellerAirParticleData(), pos.x, pos.y, pos.z, -5, 0, 0);
                    }
                }

                // Don't update position in wind tunnel
                if(scenario != WIND_TUNNEL) {
                    sectionPosition = sectionPosition.add(rigidbody.globalVelocity.scale(0.05));
                }
            }

            sectionRotation = rigidbody.orientation;
        }

        Vector3d rotation = toXYZDegrees(sectionRotation);
        section.setAnimatedRotation(rotation, false);
        section.setAnimatedOffset(sectionPosition.subtract(originalPosition), false);

        // 90 degree interval snapping
        if(!mouseDown) {
            double currentXRot = scene.getTransform().xRotation.getValue();
            double currentYRot = scene.getTransform().yRotation.getValue();

            double snapX = Math.round(currentXRot / 90) * 90;
            double snapY = Math.round(currentYRot / 90) * 90;

            // only snap if within 10 degrees
            if(Math.abs(snapX - currentXRot) < 10 && Math.abs(snapY - currentYRot) < 10) {
                scene.getTransform().xRotation.chase(snapX, 15.0f, LerpedFloat.Chaser.LINEAR);
                scene.getTransform().yRotation.chase(snapY, 15.0f, LerpedFloat.Chaser.LINEAR);
            }

        }
    }

    boolean mouseDown = false;

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        mouseDown = true;
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        mouseDown = false;
        return super.mouseReleased(x, y, button);
    }

    @Override
    protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

        partialTicks = getPartialTicks();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        int tooltipColor = Theme.i(Theme.Key.TEXT_DARKER);
        {
            // Chapter title
            ms.pushPose();
            ms.translate(0, 0, 400);
            int x = 31 + 20 + 8;
            int y = 31;

            font.draw(ms, "Test Physics Scenarios", width - 100 - 18 - 50, 26, tooltipColor);

            String title = "Airship #" + airship.plotId;
            int wordWrappedHeight = font.wordWrapHeight(title, (width / 3));

            int streakHeight = 35 - 9 + wordWrappedHeight;
            UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (150 * 1.0));
            UIRenderHelper.streak(ms, 180, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (30 * 1.0));
            new BoxElement()
                    .withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
                    .gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
                    .at(21, 21, 100)
                    .withBounds(30, 30)
                    .render(ms);


            GuiGameElement.of(new ItemStack(CABlocks.AIRSHIP_ASSEMBLER.get().asItem()))
                    .scale(2)
                    .at(x - 39, y - 11)
                    .render(ms);

            font.draw(ms, "Inspecting...", x, y - 6, tooltipColor);

            y += 8;
            x += 0;


            ms.translate(x, y, 0);
            FontHelper.drawSplitString(ms, font, title, 0, 0, (width / 3),
                    Theme.c(Theme.Key.TEXT).getRGB());
            font.draw(ms, "Disable Center Of Mass", -8,  37, Theme.c(Theme.Key.BUTTON_SUCCESS).getRGB());

            ms.popPose();

            if(inScenario()) {
                ms.pushPose();
                ms.translate(0, 0, 400);

                // we need to let the player know they're in a scenario
                UIRenderHelper.streak(ms, 0, scenarioStatementStartX, scenarioStatementY + streakHeight / 2, streakHeight, scenarioStatementWidth + 60);
//            UIRenderHelper.streak(ms, 180, scenarioStatementStartX, scenarioStatementY + streakHeight / 2, streakHeight, scenarioStatementWidth / 5);
                new BoxElement()
                        .withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
                        .gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
                        .at(scenarioStatementStartX + 1, scenarioStatementY + 1, 100)
                        .withBounds(30, 30)
                        .render(ms);


                GuiGameElement.of(new ItemStack(((Block) scenario.item.get()).asItem()))
                        .scale(2)
                        .at(scenarioStatementStartX, scenarioStatementY)
                        .render(ms);

                font.draw(ms, "Pondering Scenario...", scenarioStatementStartX + 40, scenarioStatementY + 6, tooltipColor);
                String scenarioTitle = scenario.toString().toLowerCase().replace("_", " ");
                scenarioTitle = scenarioTitle.substring(0, 1).toUpperCase() + scenarioTitle.substring(1);

                font.draw(ms, scenarioTitle, scenarioStatementStartX + 40, scenarioStatementY + 6 + 12, Theme.c(Theme.Key.TEXT).getRGB());

                ms.popPose();
            }
        }

        if(scene != null) {
            renderScene(ms, mouseX, mouseY, 0, partialTicks);
        }

        RenderSystem.disableBlend();

    }

    protected void renderScene(MatrixStack ms, int mouseX, int mouseY, int i, float partialTicks) {
        SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
        PonderScene story = scene;
        double diff = 0;
        double slide = MathHelper.lerp(diff * diff, 200, 600) * diff;
        slide = 1.0;

        MutableBoundingBox bounds = story.getBounds();
        float maxSpan = Math.max(Math.max(bounds.getXSpan(), bounds.getYSpan() - 128), bounds.getZSpan());
//        float scale = (float) (8.0 / maxSpan) * (inScenario() ? 1.0f : 1.0f);
        float scale = 0.4f;

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        RenderSystem.pushMatrix();

        // has to be outside of MS transforms, important for vertex sorting
        RenderSystem.translated(0, 0, 800);

        ms.pushPose();
        ms.translate(0, 0, -400);
        story.getTransform().updateScreenParams(width, height, slide);
        story.getTransform().apply(ms, partialTicks, false);
        story.getTransform().updateSceneRVE(partialTicks);
        ms.scale(scale, scale, scale);
        ms.translate(0, -128, 0);
        story.renderScene(buffer, ms, partialTicks);


        buffer.draw();

//        ms.pushPose();

        ms.popPose();
//        ms.popPose();
        RenderSystem.popMatrix();

        RenderSystem.disableDepthTest();
        RenderSystem.pushMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();

        RenderSystem.translated(0, 0, 800);

        ms.pushPose();
        ms.translate(0, 0, -800);
        story.getTransform().updateScreenParams(width, height, slide);
        story.getTransform().apply(ms, partialTicks, false);

        Vector3d rotation = section.getAnimatedRotation();


        //        ms.translate(0, 128, 0);
        ms.scale(scale, scale, scale);

        float size = 0.2f;
        double offset = 0.5 - size / 2;

        ms.translate(offset, offset, offset);
        ms.translate(airship.centerOfMassOffset.x, airship.centerOfMassOffset.y, airship.centerOfMassOffset.z);
        Vector3d contraptionPositionOffset = section.getAnimatedOffset();
        ms.translate(contraptionPositionOffset.x, contraptionPositionOffset.y, contraptionPositionOffset.z);


        Matrix4f model = ms.last().pose();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        cube(builder, size, 1.0, model, 230, 230, 0);

        if(scenario == WIND_TUNNEL) {
            ms.translate(0, 0, -10);
            cube(builder, size, 40.0 * 1 / offset, model, 240, 240, 240);
        }

        tessellator.end();
        ms.popPose();
        RenderSystem.popMatrix();

    }

    private void cube(BufferBuilder builder, float size, double zScale, Matrix4f model, int r, int g, int b) {
        quad(
                builder,
                model,
                new Vector3d(0, 0.0, 0.0),
                new Vector3d(0, size, 0.0),
                new Vector3d(size, size, 0.0),
                new Vector3d(size, 0.0, 0.0),
                r + 0,
                g + 0,
                b + 0
        );
        quad(
                builder,
                model,
                new Vector3d(size, 0.0, size  * zScale),
                new Vector3d(size, size, size  * zScale),
                new Vector3d(0, size, size  * zScale),
                new Vector3d(0, 0.0, size * zScale),
                r + 0,
                g + 0,
                b + 0
        );
        quad(
                builder,
                model,
                new Vector3d(0, size, 0.0),
                new Vector3d(0, size, size * zScale),
                new Vector3d(size, size, size * zScale),
                new Vector3d(size, size, 0.0),
                r + 20,
                g + 20,
                b + 20
        );
        // bottom
        quad(
                builder,
                model,
                new Vector3d(size, 0.0, 0.0),
                new Vector3d(size, 0.0, size * zScale),
                new Vector3d(0, 0.0, size * zScale),
                new Vector3d(0, 0.0, 0.0),
                r -20,
                g -20,
                b -20
        );
        // top
        quad(
                builder,
                model,
                new Vector3d(0.0, 0.0, size * zScale),
                new Vector3d(0.0, size, size * zScale),
                new Vector3d(0, size, 0.0),
                new Vector3d(0, 0.0, 0.0),
                r + 0,
                g + 0,
                b + 0
        );
        quad(
                builder,
                model,
                new Vector3d(size, 0.0, 0.0),
                new Vector3d(size, size, 0.0),
                new Vector3d(size, size, size * zScale),
                new Vector3d(size, 0.0, size * zScale),
                r + 0,
                g + 0,
                b + 0
        );
    }

    private void quad(BufferBuilder buffer, Matrix4f model, Vector3d a, Vector3d b, Vector3d c, Vector3d d, int rmod, int gmod, int bmod) {
        buffer.vertex(model, (float) a.x, (float) a.y, (float) a.z).color(Math.min(rmod, 255), Math.min(gmod, 255), Math.min(Math.max(0, 0 + bmod), 255), 0).endVertex();
        buffer.vertex(model, (float) b.x, (float) b.y, (float) b.z).color(Math.min(rmod, 255), Math.min(gmod, 255), Math.min(Math.max(0, 0 + bmod), 255), 0).endVertex();
        buffer.vertex(model, (float) c.x, (float) c.y, (float) c.z).color(Math.min(rmod, 255), Math.min(gmod, 255), Math.min(Math.max(0, 0 + bmod), 255), 0).endVertex();
        buffer.vertex(model, (float) d.x, (float) d.y, (float) d.z).color(Math.min(rmod, 255), Math.min(gmod, 255), Math.min(Math.max(0, 0 + bmod), 255), 0).endVertex();



//        b.vertex(model, 0, 0, 0).color(0, 255, 0, 0).endVertex();
//        b.vertex(model, 0, size, 0).color(0, 255, 0, 0).endVertex();
//        b.vertex(model, size, size, 0).color(0, 255, 0, 0).endVertex();
//        b.vertex(model, size, 0, 0).color(0, 255, 0, 0).endVertex();
    }

    @Override
    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double dragX, double dragY) {
        scene.getTransform().xRotation.chase(scene.getTransform().xRotation.getChaseTarget() - dragY / 3, 100.0, LerpedFloat.Chaser.LINEAR);
        scene.getTransform().yRotation.chase(scene.getTransform().yRotation.getChaseTarget() + dragX / 3, 100.0, LerpedFloat.Chaser.LINEAR);
        return super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, dragX, dragY);
    }

    @Override
    protected String getBreadcrumbTitle() {
        return "Airship Physics Info";
    }

    public static void renderSpeechBox(MatrixStack ms, int x, int y, int w, int h, boolean highlighted, Pointing pointing,
                                       boolean returnWithLocalTransform) {
        if (!returnWithLocalTransform)
            ms.pushPose();

        int boxX = x;
        int boxY = y;
        int divotX = x;
        int divotY = y;
        int divotRotation = 0;
        int divotSize = 8;
        int distance = 1;
        int divotRadius = divotSize / 2;
        Couple<Color> borderColors = Theme.p(highlighted ? Theme.Key.PONDER_HIGHLIGHT : Theme.Key.PONDER_IDLE);
        Color c;

        switch (pointing) {
            default:
            case DOWN:
                divotRotation = 0;
                boxX -= w / 2;
                boxY -= h + divotSize + 1 + distance;
                divotX -= divotRadius;
                divotY -= divotSize + distance;
                c = borderColors.getSecond();
                break;
            case LEFT:
                divotRotation = 90;
                boxX += divotSize + 1 + distance;
                boxY -= h / 2;
                divotX += distance;
                divotY -= divotRadius;
                c = Color.mixColors(borderColors, 0.5f);
                break;
            case RIGHT:
                divotRotation = 270;
                boxX -= w + divotSize + 1 + distance;
                boxY -= h / 2;
                divotX -= divotSize + distance;
                divotY -= divotRadius;
                c = Color.mixColors(borderColors, 0.5f);
                break;
            case UP:
                divotRotation = 180;
                boxX -= w / 2;
                boxY += divotSize + 1 + distance;
                divotX -= divotRadius;
                divotY += distance;
                c = borderColors.getFirst();
                break;
        }

        new BoxElement()
                .withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
                .gradientBorder(borderColors)
                .at(boxX, boxY, 100)
                .withBounds(w, h)
                .render(ms);

        ms.pushPose();
        ms.translate(divotX + divotRadius, divotY + divotRadius, 10);
//        ms.mulPose(Vector3f.ZP.rotationDegrees(divotRotation));
        ms.translate(-divotRadius, -divotRadius, 0);
        AllGuiTextures.SPEECH_TOOLTIP_BACKGROUND.draw(ms, 0, 0);
        AllGuiTextures.SPEECH_TOOLTIP_COLOR.draw(ms, 0, 0, c);
        ms.popPose();

        if (returnWithLocalTransform) {
            ms.translate(boxX, boxY, 0);
            return;
        }

        ms.popPose();

    }

}
