package me.derp.quantum.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.event.events.Render3DEvent;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.modules.client.ClickGui;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.ColorUtil;
import me.derp.quantum.util.RenderUtil;
import me.derp.quantum.util.Timer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class ChorusPredict extends Module {

    private final Timer renderTimer = new Timer();
    private BlockPos pos;
    private final Setting<Boolean> debug = this.register(new Setting("Debug", true));
    private final Setting<Integer> renderDelay = this.register(new Setting("RenderDelay", 4000, 0, 4000));
    private final Setting<Boolean> rainbow = register(new Setting("Rainbow", false));
    private final Setting<Integer> red = register(new Setting("Red", 0, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> green = register(new Setting("Green", 255, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 0, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> outlineAlpha = register(new Setting("OL-Alpha", 0, 0, 255, v -> !this.rainbow.getValue()));


    public ChorusPredict() {
        super("ChorusView", "Predicts Chorus", Category.COMBAT, true, false, false);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            final SPacketSoundEffect packet = event.getPacket();
            if (packet.getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT || packet.getSound() == SoundEvents.ENTITY_ENDERMEN_TELEPORT) {
                renderTimer.reset2();
                pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                if (this.debug.getValue()) {
                    Command.sendMessage("A player chorused to: " + ChatFormatting.AQUA + "X: " + pos.getX() + ", Y: " + pos.getY() + ", Z: " + pos.getZ());
                }
            }

        }
    }

    public void onRender3D(Render3DEvent event) {
        if (pos != null) {
            if (renderTimer.passed(this.renderDelay.getValue())) {
                pos = null;
                return;
            }
            RenderUtil.drawBoxESP(pos, rainbow.getValue() ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(red.getValue(), green.getValue(), blue.getValue(), outlineAlpha.getValue()), 1.5F, true, true, alpha.getValue());
        }
    }

}