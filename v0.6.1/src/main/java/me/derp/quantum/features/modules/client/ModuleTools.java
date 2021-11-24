package me.derp.quantum.features.modules.client;

import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;

public class ModuleTools extends Module {

    private static ModuleTools INSTANCE;

    public Setting<Notifier> notifier = register(new Setting("ModuleNotifier", Notifier.FUTURE));
    public Setting<PopNotifier> popNotifier = register(new Setting("PopNotifier", PopNotifier.FUTURE));

    public ModuleTools() {
        super("ModuleTools", "Change settings", Module.Category.CLIENT, true, false, false);
        INSTANCE = this;
    }


    public static ModuleTools getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModuleTools();
        }
        return INSTANCE;
    }


    public enum Notifier {
        TROLLGOD,
        PHOBOS,
        FUTURE,
        DOTGOD
    }

    public enum PopNotifier {
        PHOBOS,
        FUTURE,
        DOTGOD,
        NONE
    }


}
