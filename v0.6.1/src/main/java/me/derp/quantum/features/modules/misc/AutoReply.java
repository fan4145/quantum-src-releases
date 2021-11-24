package me.derp.quantum.features.modules.misc;

import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.modules.Module;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoReply extends Module {


    public AutoReply() {
        super("AutoReply", "replys to msgs automatically", Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Receive event){
        if (event.getPacket() instanceof SPacketChat) {
            final SPacketChat packet = (SPacketChat) event.getPacket();
            if (packet.getChatComponent() instanceof TextComponentString) {
                final String component =  packet.getChatComponent().getFormattedText();
                if (component.toLowerCase().contains("whispers: ")){
                    mc.player.sendChatMessage("/r lol, i am afking rn with the best client in 2b2t community called Quantum Continued | https://discord.gg/mBGB4pdXqk ");
                }
            }


        }


    }

}