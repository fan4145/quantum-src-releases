package me.derp.quantum.features.modules.troll;

import me.derp.quantum.features.modules.Module;
import net.minecraft.network.play.client.CPacketChatMessage;


public class Suicide extends Module {
    public Suicide() {
        super("suicide", "for lazy ppl who cant type /kill", Category.TROLL, true, false, false);

    }

    @Override
    public
    void onEnable() {
        if (fullNullCheck()) return;
                mc.player.connection.sendPacket(new CPacketChatMessage("/kill"));
    }
}

