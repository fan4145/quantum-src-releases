package me.derp.quantum.features.modules.misc;

import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Autorespawn
        extends Module {
    public Setting<Boolean> antiDeathScreen = this.register(new Setting<Boolean>("AntiDeathScreen", true));
    public Setting<Boolean> deathCoords = this.register(new Setting<Boolean>("DeathCoords", false));
    public Setting<Boolean> respawn = this.register(new Setting<Boolean>("Respawn", true));

    public Autorespawn() {
        super("AutoRespawn", "Respawns you when you die.", Module.Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onDisplayDeathScreen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiGameOver) {
            if (this.deathCoords.getValue() && event.getGui() instanceof GuiGameOver) {
                Command.sendMessage(String.format("You died at x %d y %d z %d", (int)Autorespawn.mc.player.posX, (int)Autorespawn.mc.player.posY, (int)Autorespawn.mc.player.posZ));
            }
            if (this.respawn.getValue() && Autorespawn.mc.player.getHealth() <= 0.0f || this.antiDeathScreen.getValue() && Autorespawn.mc.player.getHealth() > 0.0f) {
                event.setCanceled(true);
                Autorespawn.mc.player.respawnPlayer();
            }
        }
    }
}
