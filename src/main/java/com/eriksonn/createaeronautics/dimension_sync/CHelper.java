package com.eriksonn.createaeronautics.dimension_sync;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CHelper {
    private static int reportedErrorNum = 0;

    public CHelper() {
    }

    public static NetworkPlayerInfo getClientPlayerListEntry() {
        return Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getGameProfile().getId());
    }

    //public static boolean shouldDisableFog() {
    //    return OFInterface.shouldDisableFog.getAsBoolean();
    //}

    //public static World getClientWorld(RegistryKey<World> dimension) {
    //    return ClientWorldLoader.getWorld(dimension);
    //}



    //public static void checkGlError() {
    //    if (Global.doCheckGlError) {
    //        if (reportedErrorNum <= 100) {
    //            int errorCode = GL11.glGetError();
    //            if (errorCode != 0) {
    //                Helper.err("OpenGL Error" + errorCode);
    //                (new Throwable()).printStackTrace();
    //                ++reportedErrorNum;
    //            }
//
    //        }
    //    }
    //}

    public static void printChat(String str) {
        printChat(new StringTextComponent(str));
    }

    public static void printChat(StringTextComponent text) {
        Minecraft.getInstance().gui.getChat().addMessage(text);
    }

    public static void openLinkConfirmScreen(Screen parent, String link) {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new ConfirmOpenLinkScreen((result) -> {
            if (result) {
                try {
                    Util.getPlatform().openUri(new URI(link));
                } catch (URISyntaxException var5) {
                    var5.printStackTrace();
                }
            }

            client.setScreen(parent);
        }, link, true));
    }

    public static Vector3d getCurrentCameraPos() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    }

    //public static <T> T withWorldSwitched(Entity entity, Portal portal, Supplier<T> func) {
    //    World oldWorld = entity.level;
    //    Vector3d eyePos = McHelper.getEyePos(entity);
    //    Vector3d lastTickEyePos = McHelper.getLastTickEyePos(entity);
    //    entity.level = portal.getDestinationWorld();
    //    McHelper.setEyePos(entity, portal.transformPoint(eyePos), portal.transformPoint(lastTickEyePos));
//
    //    Object var7;
    //    try {
    //        T result = func.get();
    //        var7 = result;
    //    } finally {
    //        entity.level = oldWorld;
    //        McHelper.setEyePos(entity, eyePos, lastTickEyePos);
    //    }
//
    //    return var7;
    //}

    public static Iterable<Entity> getWorldEntityList(World world) {
        if (!(world instanceof ClientWorld)) {
            return (Iterable) Collections.emptyList().iterator();
        } else {
            ClientWorld clientWorld = (ClientWorld)world;
            return clientWorld.entitiesForRendering();
        }
    }

    public static void executeOnRenderThread(Runnable runnable) {
        Minecraft client = Minecraft.getInstance();
        if (client.isSameThread()) {
            runnable.run();
        } else {
            client.execute(runnable);
        }

    }

    //public static double getSmoothCycles(long unitTicks) {
    //    int playerAge = Minecraft.getInstance().player.tickCount;
    //    return (double)((float)((long)playerAge % unitTicks) + RenderStates.tickDelta) / (double)unitTicks;
    //}
}
