package me.derp.quantum.features.gui.components.items.buttons;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.gui.OyVeyGui;
import me.derp.quantum.features.gui.components.Component;
import me.derp.quantum.features.gui.components.items.Item;
import me.derp.quantum.features.modules.client.ClickGui;
import me.derp.quantum.util.RenderUtil;
import me.derp.quantum.util.Util;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;

public class Button
        extends Item {
    private boolean state;

    public Button(String name) {
        super(name);
        this.height = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Quantum.moduleManager.isModuleEnabled(this.getName())) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float) this.width, this.y + (float) this.height + 0.5f, new Color(130, 0, 255, 100).hashCode());
            Quantum.textManager.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float) OyVeyGui.getClickGui().getTextOffset(), new Color(255, 255, 255, 255).hashCode());
        } else {
            if (isHovering(mouseX, mouseY)) {
                RenderUtil.drawRect(this.x, this.y, this.x + (float) this.width, this.y + (float) this.height + 0.5f, new Color(30,30,30,127).hashCode());
            } else {
                RenderUtil.drawRect(this.x, this.y, this.x + (float) this.width, this.y + (float) this.height + 0.5f, new Color(30,30,30,100).hashCode());
            }
            Quantum.textManager.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float) OyVeyGui.getClickGui().getTextOffset(), new Color(255, 255, 255).hashCode());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        Util.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : OyVeyGui.getClickGui().getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float) mouseX >= this.getX() && (float) mouseX <= this.getX() + (float) this.getWidth() && (float) mouseY >= this.getY() && (float) mouseY <= this.getY() + (float) this.height;
    }
}

