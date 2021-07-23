package me.alpha432.oyvey;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.alpha432.oyvey.features.modules.misc.RPC;
import net.minecraft.client.Minecraft;

public class DiscordPresence {
    public static DiscordRichPresence presence;
    private static Thread thread;


    public static void start() {
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        rpc.Discord_Initialize("866963014314098749", handlers, true, "");
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.details = ((Minecraft.getMinecraft()).currentScreen instanceof net.minecraft.client.gui.GuiMainMenu) ? "Playing: Browsing server list." : ("Playing " + ((Minecraft.getMinecraft().getCurrentServerData() != null) ? (((Boolean)RPC.INSTANCE.showIP.getValue()).booleanValue() ? (": " + (Minecraft.getMinecraft().getCurrentServerData()).serverIP + ".") : " multiplayer.") : "singleplayer."));
        presence.state = (String)RPC.INSTANCE.state.getValue();
        presence.largeImageKey = "q2";
        presence.largeImageText = "Quantum continued v0.5.0";
        rpc.Discord_UpdatePresence(presence);
        (thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                rpc.Discord_RunCallbacks();
                String string = "";
                StringBuilder sb = new StringBuilder();
                DiscordRichPresence presence = DiscordPresence.presence;
                (new StringBuilder()).append("Playing");
                if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                    if (((Boolean)RPC.INSTANCE.showIP.getValue()).booleanValue()) {
                        string = ": " + (Minecraft.getMinecraft().getCurrentServerData()).serverIP + ".";
                    } else {
                        string = " multiplayer.";
                    }
                } else {
                    string = " singleplayer.";
                }
                presence.details = sb.append(string).toString();
                DiscordPresence.presence.state = (String)RPC.INSTANCE.state.getValue();
                rpc.Discord_UpdatePresence(DiscordPresence.presence);
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException interruptedException) {}
            }
        },"RPC-Callback-Handler")).start();
    }

    public static void stop() {
        if (thread != null && !thread.isInterrupted())
            thread.interrupt();
        rpc.Discord_Shutdown();
    }

    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;



    static {
        presence = new DiscordRichPresence();
    }
}
