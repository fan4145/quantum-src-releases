package me.derp.quantum.features.modules.misc;

import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatModifier
        extends Module {
    public Setting<Suffix> suffix = this.register(new Setting<>("Suffix", Suffix.NONE, "Your Suffix."));
    public Setting<Boolean> clean = this.register(new Setting<>("CleanChat", Boolean.FALSE, "Cleans your chat"));
    public Setting<Boolean> infinite = this.register(new Setting<>("Infinite", Boolean.FALSE, "Makes your chat infinite."));
    public Setting<Boolean> autoQMain = this.register(new Setting<>("AutoQMain", Boolean.FALSE, "Spams AutoQMain"));
    public Setting<Boolean> qNotification = this.register(new Setting<Object>("QNotification", Boolean.FALSE, v -> this.autoQMain.getValue()));
    public Setting<Integer> qDelay = this.register(new Setting<Object>("QDelay", 9, 1, 90, v -> this.autoQMain.getValue()));
    private final Timer timer = new Timer();
    private static ChatModifier INSTANCE = new ChatModifier();

    public ChatModifier() {
        super("Chat", "Modifies your chat", Module.Category.MISC, true, false, false);
        this.setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static ChatModifier getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatModifier();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (this.autoQMain.getValue()) {
            if (!this.shouldSendMessage(ChatModifier.mc.player)) {
                return;
            }
            if (this.qNotification.getValue()) {
                Command.sendMessage("<AutoQueueMain> Sending message: /queue main");
            }
            ChatModifier.mc.player.sendChatMessage("/queue main");
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = event.getPacket();
            String s = packet.getMessage();
            if (s.startsWith("/")) {
                return;
            }
            if (this.suffix.getValue() == Suffix.Quantum) {
                s = s + " \u23d0 \uff51\uff55\uff41\uff4e\uff54\uff55\uff4d\u3000 \uff43\uff4f\uff4e\uff54\uff49\uff4e\uff55\uff45\uff44";
            }
            if (s.length() >= 256) {
                s = s.substring(0, 256);
            }
            packet.message = s;
        }
    }


    private boolean shouldSendMessage(EntityPlayer player) {
        if (player.dimension != 1) {
            return false;
        }
        if (!this.timer.passedS(this.qDelay.getValue())) {
            return false;
        }
        return player.getPosition().equals(new Vec3i(0, 240, 0));
    }

    public
    enum Suffix {
        NONE,
        Quantum

    }
}