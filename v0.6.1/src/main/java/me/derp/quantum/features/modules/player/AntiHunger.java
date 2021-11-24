package me.derp.quantum.features.modules.player;

import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiHunger extends Module {
    public Setting<Boolean> cancelSprint = register(new Setting<>("CancelSprint", true));
    public Setting<Boolean> ground = register(new Setting<>("Ground", true));

    public AntiHunger() {
        super("AntiHunger", "Prevents you from getting Hungry.", Category.PLAYER, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (this.ground.getValue() && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = event.getPacket();
            packet.onGround = (mc.player.fallDistance >= 0.0F || mc.playerController.isHittingBlock);
        }
        if (this.cancelSprint.getValue() && event.getPacket() instanceof CPacketEntityAction) {
            CPacketEntityAction packet = event.getPacket();
            if (packet.getAction() == CPacketEntityAction.Action.START_SPRINTING || packet.getAction() == CPacketEntityAction.Action.STOP_SPRINTING)
                event.setCanceled(true);
        }
    }
}