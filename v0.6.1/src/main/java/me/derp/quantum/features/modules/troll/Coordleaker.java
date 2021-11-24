package me.derp.quantum.features.modules.troll;

import me.derp.quantum.features.modules.Module;
import net.minecraft.network.play.client.CPacketChatMessage;


public class Coordleaker extends Module {
    public Coordleaker() {
        super("Coordleaker", "troll 69", Category.TROLL, true, false, false);

    }

    @Override
    public
    void onEnable() {
        if (fullNullCheck()) return;
        mc.player.connection.sendPacket(new CPacketChatMessage("lol my coords are: " + Math.floor(mc.player.posX) + ", " + Math.floor(mc.player.posY) + ", " + Math.floor(mc.player.posZ) + "! come and kill me."));
    }
}
