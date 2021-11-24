package me.derp.quantum.features.modules.player;

import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;

public class NoEntityTrace extends Module {

    public static NoEntityTrace INSTANCE;
    public Setting<Boolean> pickaxe = this.register(new Setting("Pickaxe", true));

    public NoEntityTrace() {
        super("NoEntityTrace", "No trace", Category.PLAYER, true, false, false);
        this.setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static NoEntityTrace getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoEntityTrace();
        }
        return INSTANCE;
    }


}
