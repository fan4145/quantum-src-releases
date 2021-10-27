package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class Logo extends Module {
    public static final ResourceLocation mark = new ResourceLocation("Quantum.png");
    public Setting<Integer> imageX;
    public Setting<Integer> imageY;
    public Setting<Integer> imageWidth;
    public Setting<Integer> imageHeight;
    private int color;

    public Logo() {
        super("Logo", "Puts a logo there (there)", Category.CLIENT, false, false, false);
        this.imageX = this.register(new Setting("WatermarkX", 0, 0, 300));
        this.imageY = this.register(new Setting("WatermarkY", 0, 0, 300));
        this.imageWidth = this.register(new Setting("WatermarkWidth", 97, 0, 1000));
        this.imageHeight = this.register(new Setting("WatermarkHeight", 97, 0, 1000));

    }

    public void renderLogo() {
        int width = this.imageWidth.getValue();
        int height = this.imageHeight.getValue();
        int x = this.imageX.getValue();
        int y = this.imageY.getValue();
        mc.renderEngine.bindTexture(mark);
        GlStateManager.color(255.0F, 255.0F, 255.0F);
        Gui.drawScaledCustomSizeModalRect(x - 2, y - 36, 7.0F, 7.0F, width - 7, height - 7, width, height, (float) width, (float) height);
    }

    public void onRender2D(Render2DEvent event) {
        if (!fullNullCheck()) {
            int width = this.renderer.scaledWidth;
            int height = this.renderer.scaledHeight;
            this.color = ColorUtil.toRGBA(ClickGui.getInstance().red.getValue(), ClickGui.getInstance().green.getValue(), ClickGui.getInstance().blue.getValue());
            if (this.enabled.getValue()) {
                this.renderLogo();
            }

        }

    }
}