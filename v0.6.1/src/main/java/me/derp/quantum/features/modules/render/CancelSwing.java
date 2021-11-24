package me.derp.quantum.features.modules.render;

import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CancelSwing extends Module {

    public CancelSwing() {
        super("Swing", "si", Category.PLAYER, true, false, false);
        register(switchSetting);
        register(swing);
    }

    public Setting<Switch> switchSetting = new Setting<>("Switch", Switch.ONEDOTEIGHT);
    public Setting<Swing> swing = new Setting<>("Swing", Swing.MAINHAND);

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketAnimation) {
            if (swing.getValue() == Swing.MAINHAND) mc.player.swingingHand = EnumHand.MAIN_HAND;
            if (swing.getValue() == Swing.OFFHAND) mc.player.swingingHand = EnumHand.OFF_HAND;
            if (swing.getValue() == Swing.CANCEL) event.setCanceled(true);
        }
    }
    @Override
    public void onTick() {
        if (fullNullCheck()) return;
        if (switchSetting.getValue() == Switch.ONEDOTEIGHT && mc.entityRenderer.itemRenderer.prevEquippedProgressMainHand >= 0.9) {
            mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0f;
            mc.entityRenderer.itemRenderer.itemStackMainHand = mc.player.getHeldItemMainhand();
        }
    }

    public enum Switch {
        ONEDOTEIGHT, ONEDOTNINE
    }
    public enum Swing {
        MAINHAND, OFFHAND, CANCEL
    }
}