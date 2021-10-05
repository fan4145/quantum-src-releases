package me.alpha432.oyvey.features.modules.misc;


import me.alpha432.oyvey.DiscordPresence;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class RPC
        extends Module {
    public static RPC INSTANCE;
    public Setting<String> state = this.register(new Setting<>("State", "Quantum Continued v0.6.0", "Sets the state of the DiscordRPC."));
    public Setting<Boolean> showIP = this.register(new Setting<>("ShowIP", Boolean.TRUE, "Shows the server IP in your discord presence."));
    public Setting<Boolean> catMode = this.register(new Setting<>("SnineMode", false, "cute cat supremacy"));

    public RPC() {
        super("RPC", "Discord rich presence", Module.Category.MISC, false, false, false);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        DiscordPresence.start();
    }

    @Override
    public void onDisable() {
        DiscordPresence.stop();
    }
}
