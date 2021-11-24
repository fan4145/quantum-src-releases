package me.derp.quantum.features.modules.combat;

import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;

import java.util.Objects;

public class FakeKick
        extends Module {
    private final Setting<Boolean> healthDisplay = this.register(new Setting<>("HealthDisplay", false));

    public FakeKick() {
        super("FakeKick", "Log with the press of a button", Category.COMBAT, true, false, false);
    }

    public void onEnable() {
        if (healthDisplay.getValue()) {
            float health = (mc.player.getAbsorptionAmount() + mc.player.getHealth());
            Objects.requireNonNull(Minecraft.getMinecraft().getConnection()).handleDisconnect(new SPacketDisconnect(new TextComponentString("Logged out with " + health + " health remaining.")));
            this.disable();
        }
        Objects.requireNonNull(Minecraft.getMinecraft().getConnection()).handleDisconnect(new SPacketDisconnect(new TextComponentString("Internal Exception: java.lang.NullPointerException")));
        this.disable();
    }
}