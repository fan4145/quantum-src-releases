package me.derp.quantum.features.modules.client;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.util.IconUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.lwjgl.opengl.Display;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class Icon extends Module {

    private static Icon INSTANCE = new Icon();

    public Icon() {
        super("Icon", "Toggle Icon", Category.CLIENT, true, false, false);
        setInstance();
    }

    public static Icon getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Icon();
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        setWindowsIcon();
        Command.sendMessage("Restart your game for this to take effect.");
    }

    @Override
    public void onDisable() {
        mc.setWindowIcon();
    }

    public static void setWindowIcon() {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            try (InputStream inputStream16x = Minecraft.class.getResourceAsStream("/assets/Quantum/icons/icon-16x.png");
                 InputStream inputStream32x = Minecraft.class.getResourceAsStream("/assets/Quantum/icons/icon-32x.png")) {
                ByteBuffer[] icons = new ByteBuffer[]{IconUtil.INSTANCE.readImageToBuffer(inputStream16x), IconUtil.INSTANCE.readImageToBuffer(inputStream32x)};
                Display.setIcon(icons);
            } catch (Exception e) {
                Quantum.LOGGER.error("Couldn't set Windows Icon", e);
            }
        }
    }

    private void setWindowsIcon() {
        setWindowIcon();
    }
}
