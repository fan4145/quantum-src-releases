package me.derp.quantum.features.modules.client;

import me.derp.quantum.Quantum;
import me.derp.quantum.event.events.Render2DEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.ColorUtil;
import me.derp.quantum.util.RenderUtil;
import me.derp.quantum.util.Timer;

public class CSGOWatermark extends Module {

    Timer delayTimer = new Timer();
    public Setting<Integer> X = this.register(new Setting("WatermarkX", 0, 0, 300));
    public Setting<Integer> Y = this.register(new Setting("WatermarkY", 0, 0, 300));
    public Setting<Integer> delay = this.register(new Setting<Object>("Delay", 240, 0, 600));
    public Setting<Integer> saturation = this.register(new Setting<Object>("Saturation", 127, 1, 255));
    public Setting<Integer> brightness = this.register(new Setting<Object>("Brightness", 100, 0, 255));
    public float hue;
    public int red = 1;
    public int green = 1;
    public int blue = 1;

    public CSGOWatermark() {
        super("CSGOWatermark", "noat em cee actually makes something", Module.Category.CLIENT, true, false, false);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        drawCsgoWatermark();
    }

    public void drawCsgoWatermark() {
        int padding = 5;
        String message = "Quantum Continued v" + Quantum.MODVER + " | " + mc.player.getName() + " | " + Quantum.serverManager.getPing() + "ms";
        int textWidth = Quantum.textManager.getStringWidth(message); // taken from wurst+ 3
        int textHeight = mc.fontRenderer.FONT_HEIGHT; // taken from wurst+ 3
        RenderUtil.drawRectangleCorrectly(X.getValue() - 4, Y.getValue() - 4, textWidth + 16, textHeight + 12, ColorUtil.toRGBA(22, 22, 22, 255));
        RenderUtil.drawRectangleCorrectly(X.getValue(), Y.getValue(), textWidth + 4, textHeight + 4, ColorUtil.toRGBA(0, 0, 0, 255));
        RenderUtil.drawRectangleCorrectly(X.getValue(), Y.getValue(), textWidth + 8, textHeight + 4, ColorUtil.toRGBA(0, 0, 0, 255));
        RenderUtil.drawRectangleCorrectly(X.getValue(), Y.getValue(), textWidth + 8, 1, ColorUtil.rainbow(this.delay.getValue()).hashCode());
        Quantum.textManager.drawString(message, X.getValue() + 3, Y.getValue() + 3, ColorUtil.toRGBA(255, 255, 255, 255), false);
    }
}
