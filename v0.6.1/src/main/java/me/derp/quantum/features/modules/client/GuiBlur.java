package me.derp.quantum.features.modules.client;

import me.derp.quantum.features.gui.OyVeyGui;
import me.derp.quantum.features.modules.Module;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;

public class GuiBlur extends Module {
    public GuiBlur(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening) {
        super(name, description, category, hasListener, hidden, alwaysListening);
    }

    public void onUpdate() {
        if (GuiBlur.mc.world != null) {
            if (!ClickGui.getInstance().isEnabled() && !(GuiBlur.mc.currentScreen instanceof GuiContainer) && !(GuiBlur.mc.currentScreen instanceof GuiChat) && !(GuiBlur.mc.currentScreen instanceof GuiConfirmOpenLink) && !(GuiBlur.mc.currentScreen instanceof GuiEditSign) && !(GuiBlur.mc.currentScreen instanceof GuiGameOver) && !(GuiBlur.mc.currentScreen instanceof GuiOptions) && !(GuiBlur.mc.currentScreen instanceof GuiIngameMenu) && !(GuiBlur.mc.currentScreen instanceof GuiVideoSettings) && !(GuiBlur.mc.currentScreen instanceof GuiScreenOptionsSounds) && !(GuiBlur.mc.currentScreen instanceof GuiControls) && !(GuiBlur.mc.currentScreen instanceof GuiCustomizeSkin) && !(GuiBlur.mc.currentScreen instanceof GuiModList) && !(GuiBlur.mc.currentScreen instanceof OyVeyGui)) {
                if (GuiBlur.mc.entityRenderer.getShaderGroup() != null) {
                    GuiBlur.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
                }
            } else if (OpenGlHelper.shadersSupported && GuiBlur.mc.getRenderViewEntity() instanceof EntityPlayer) {
                if (GuiBlur.mc.entityRenderer.getShaderGroup() != null) {
                    GuiBlur.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
                }

                try {
                    GuiBlur.mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else if (GuiBlur.mc.entityRenderer.getShaderGroup() != null && GuiBlur.mc.currentScreen == null) {
                GuiBlur.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            }
        }

    }
}
