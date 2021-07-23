// plan for a while lol

/*
package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.combat.AutoCrystal;
import me.alpha432.oyvey.features.modules.combat.Killaura;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.MathUtil;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Objects;

public class TargetHUD extends Module {
  public TargetHUD() {
        super("TargetHUD", "gameing :sunglasses:", Module.Category.CLIENT, true, false, false);
  }

  private Image image;

  public Setting < Integer > targetHudX = this.register ( new Setting < Object > ( "TargetHudX" , 2 , 0 , 1000));
  public Setting < Integer > targetHudY = this.register ( new Setting < Object > ( "TargetHudY" , 2 , 0 , 1000));


    public
    EntityPlayer getClosestEnemy ( ) {
        EntityPlayer closestPlayer = null;
        for (EntityPlayer player : TargetHUD.mc.world.playerEntities) {
            if ( player == TargetHUD.mc.player || OyVey.friendManager.isFriend ( player ) ) continue;
            if ( closestPlayer == null ) {
                closestPlayer = player;
                continue;
            }
            if ( ! ( TargetHUD.mc.player.getDistanceSq ( player ) < TargetHUD.mc.player.getDistanceSq ( closestPlayer ) ) )
                continue;
            closestPlayer = player;
        }
        return closestPlayer;
    }

    @Override
    public
    void onRender2D ( Render2DEvent event ) {
      this.drawTargetHud(event.partialTicks);
    }

    public void getHead() {
        try {
          URL url = new URL("https://crafatar.com/avatars/" + getClosestEnemy.getUUID() + "?overlay=true?size=64");
          image = ImageIO.read(url);
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    public void drawTargetHud( float partialTicks ) {
        mc.renderEngine.bindTexture(image);
        GlStateManager.color(255.0F, 255.0F, 255.0F);
        Gui.drawTexturedModalRect(this.targetHudX.getValue() + this.targetHudY.getValue() - 5, 0, 0, 64, 64);
        RenderUtil.drawRectangleCorrectly ( this.targetHudX.getValue ( ) , this.targetHudY.getValue ( ) , 110 , 80 , ColorUtil.toRGBA ( 20 , 20 , 20 , 160 ) );
    }
}
*/
