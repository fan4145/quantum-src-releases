package me.derp.quantum.features.modules.render;

import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class SkyColor extends Module {

    private final Setting<Integer> red = register(new Setting("Red", 255, 0, 255));
    private final Setting<Integer> green = register(new Setting("Green", 255, 0, 255));
    private final Setting<Integer> blue = register(new Setting("Blue", 255, 0, 255));
    private final Setting<Boolean> rainbow = register(new Setting("Rainbow", false));
    private final Setting<Boolean> fog = register(new Setting("Fog", true));

    private static SkyColor INSTANCE = new SkyColor();

    public SkyColor() {
        super("SkyColor", "Changes the color of the sky", Module.Category.RENDER, false, false, false);
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static SkyColor getInstance() {
        if (INSTANCE == null)
            INSTANCE = new SkyColor();
        return INSTANCE;
    }

    @SubscribeEvent
    public void fogColors(final EntityViewRenderEvent.FogColors event) {
        event.setRed(red.getValue() / 255f);
        event.setGreen(green.getValue() / 255f);
        event.setBlue(blue.getValue() / 255f);
    }

    @SubscribeEvent
    public void fog_density(final EntityViewRenderEvent.FogDensity event) {

        if (fog.getValue()) {
            event.setDensity(0.0f);
            event.setCanceled(true);
        }
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void onUpdate() {
        if (rainbow.getValue()) {
            doRainbow();
        }
    }

    public void doRainbow() {

        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        red.setValue((color_rgb_o >> 16) & 0xFF);
        green.setValue((color_rgb_o >> 8) & 0xFF);
        blue.setValue(color_rgb_o & 0xFF);
    }


}