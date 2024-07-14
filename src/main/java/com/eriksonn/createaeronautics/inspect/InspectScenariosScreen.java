package com.eriksonn.createaeronautics.inspect;

import com.eriksonn.createaeronautics.contraptions.AirshipContraptionEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.ponder.NavigatableSimiScreen;
import com.simibubi.create.foundation.ponder.ui.LayoutHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
import com.simibubi.create.foundation.utility.FontHelper;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import java.util.Optional;

public class InspectScenariosScreen  extends NavigatableSimiScreen {


    private PonderButton windTunnelButton, bouyancyTestButton, balanceTestButton;

    public static InspectScenariosScreen of(AirshipContraptionEntity movedContraption) {
        return new InspectScenariosScreen(movedContraption);
    }

    AirshipContraptionEntity airship;

    private InspectScenariosScreen(AirshipContraptionEntity movedContraption) {
        this.airship = movedContraption;
    }
    @Override
    protected void init() {
        widgets.clear();
        super.init();


        int rowCount = MathHelper.clamp((int) Math.ceil(3 / 11d), 1, 3);
        LayoutHelper layout = LayoutHelper.centeredHorizontal(3, rowCount, 28, 28, 8);
        int itemCenterX = (int) (width * 0.5);
        int itemCenterY = (int) (0.15 * height + 45);

        // wind tunnel
        windTunnelButton =
                new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4).showing(new ItemStack(AllBlocks.SAIL.get()))
                        .withCallback(() -> {
                            ScreenOpener.transitionTo(InspectUI.of(airship, PhysicsScenario.WIND_TUNNEL));
                        });;
        widgets.add(windTunnelButton);
        layout.next();

        // balance test
        balanceTestButton =
                new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4).showing(new ItemStack(AllBlocks.TURNTABLE.get()))
                        .withCallback(() -> {
                            ScreenOpener.transitionTo(InspectUI.of(airship, PhysicsScenario.BALANCE));
                        });
        widgets.add(balanceTestButton);
        layout.next();

        // bouyancy test
        bouyancyTestButton =
                new PonderButton(itemCenterX + layout.getX() + 4, itemCenterY + layout.getY() + 4).showing(new ItemStack(Items.WATER_BUCKET))
                        .withCallback(() -> {
                            ScreenOpener.transitionTo(InspectUI.of(airship, PhysicsScenario.BOUYANCY));
                        });;
        widgets.add(bouyancyTestButton);
        layout.next();

        widgets.add(backTrack = new PonderButton(31, height - 31 - 20).enableFade(0, 5)
                .showing(AllIcons.I_MTD_CLOSE)
                .withCallback(() -> ScreenOpener.openPreviousScreen(this, Optional.empty())));
        backTrack.fade(1);

    }

    @Override
    protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        ms.pushPose();
        ms.translate(width / 2 - 120, height * 0.15 - 40, 0);

        ms.pushPose();
        // ms.translate(0, 0, 800);
        int x = 31 + 20 + 8;
        int y = 31;

        String title = "Physics Scenarios";

        int streakHeight = 35;
        UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, 240);
        // PonderUI.renderBox(ms, 21, 21, 30, 30, false);
        new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
                .gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
                .at(21, 21, 100)
                .withBounds(30, 30)
                .render(ms);

        font.draw(ms, title, x + 8, y + 1, Theme.i(Theme.Key.TEXT));
//		y += 8;
//		x += 0;
//		ms.translate(x, y, 0);
//		ms.translate(0, 0, 5);
//		textRenderer.draw(ms, title, 0, 0, Theme.i(Theme.Key.TEXT));
        ms.popPose();

        ms.pushPose();
        ms.translate(23, 23, 10);
        ms.scale(1.66f, 1.66f, 1.66f);
        ms.translate(-4, -4, 0);
        ms.scale(1.5f, 1.5f, 1.5f);
        RenderElement.of(AllIcons.I_PLACEMENT_SETTINGS).render(ms);
        ms.popPose();
        ms.popPose();

        ms.pushPose();
        int w = (int) (width * .45);
        x = (width - w) / 2;
        y = ((int)(0.15 * height + 45)) - 10 + Math.max(28, 48);

        String desc = "Physics Scenarios can be used to test various aspects of your contraptions easily";
        int h = font.wordWrapHeight(desc, w);

        // PonderUI.renderBox(ms, x - 3, y - 3, w + 6, h + 6, false);
        new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
                .gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
                .at(x - 3, y - 3, 90)
                .withBounds(w + 6, h + 6)
                .render(ms);

        ms.translate(0, 0, 100);
        FontHelper.drawSplitString(ms, font, desc, x, y, w, Theme.i(Theme.Key.TEXT));
        ms.popPose();

    }

    private PhysicsScenario hoveredItem = null;


    @Override
    public void tick() {
        super.tick();

        hoveredItem = null;
        MainWindow w = minecraft.getWindow();
        double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
        for (Widget widget : widgets) {
            if (widget == backTrack)
                continue;
            if (widget == windTunnelButton || widget == balanceTestButton || widget == bouyancyTestButton) {
                if (widget.isMouseOver(mouseX, mouseY))
                    if(widget == windTunnelButton) {
                        hoveredItem = PhysicsScenario.WIND_TUNNEL;
                    } else if(widget == balanceTestButton) {
                        hoveredItem = PhysicsScenario.BALANCE;
                    } else if(widget == bouyancyTestButton) {
                        hoveredItem = PhysicsScenario.BOUYANCY;
                    } else {
                        hoveredItem = null;
                    }
                }
        }
    }

    @Override
    protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

        if (hoveredItem == null)
            return;

        ms.pushPose();
        ms.translate(0, 0, 200);

        String text = hoveredItem.text;
        renderTooltip(ms, new StringTextComponent(text), mouseX, mouseY);

        ms.popPose();
    }


}
