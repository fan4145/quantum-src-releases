package me.derp.quantum.features.modules.misc;

import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.modules.Module;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GreenText extends Module {
    public Boolean suffix = true;
    public String s;

    public GreenText() {
        super("GreenText", "green.", Module.Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = event.getPacket();
            String s = packet.getMessage();
            if (s.startsWith("/")) {
                return;
            }
            s = "\u003e " + s;
            if (s.length() >= 256) {
                s = s.substring(0, 256);
            }
            packet.message = s;
        }
    }

    @SubscribeEvent
    public void onChatPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() == 0) {
            event.getPacket();
        }// empty if block
    }
}
