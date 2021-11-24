package me.derp.quantum.features.modules.movement;

import me.derp.quantum.features.modules.Module;
import net.minecraft.client.settings.KeyBinding;

public class AutoWalk
        extends Module {
    public AutoWalk() {
        super("AutoWalk", "Automatically walks in a straight line", Module.Category.MOVEMENT, true, false, false);
    }

    // if this module is turned on, the walk button is held down
    @Override
    public void onUpdate() {
        KeyBinding.setKeyBindState(AutoWalk.mc.gameSettings.keyBindForward.getKeyCode(), true);
    }

    @Override
    public void onDisable() {                                                              //now if the module is off we set it to false
        KeyBinding.setKeyBindState(AutoWalk.mc.gameSettings.keyBindForward.getKeyCode(), false);
    }
}


//place holder as of rn
