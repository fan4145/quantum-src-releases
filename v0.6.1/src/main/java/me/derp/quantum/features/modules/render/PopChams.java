package me.derp.quantum.features.modules.render;

import com.mojang.authlib.GameProfile;
import me.derp.quantum.event.events.Render3DEvent;
import me.derp.quantum.event.events.TotemPopEvent;
import me.derp.quantum.features.modules.Module;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class PopChams extends Module {
    public PopChams(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening) {
        super(name, description, category, hasListener, hidden, alwaysListening);
    }
    public static Color color;
    public static Color outlineColor;
    public static EntityOtherPlayerMP player;
    public static EntityPlayer entity;
    public long startTime;
    public static float opacity;
    public static long time;
    public static long duration;
    public static float startAlpha;


    @SubscribeEvent
    public void onPopped(TotemPopEvent event) {
        if (mc.player != null && mc.world != null) {
            EntityPlayer entity = event.getEntity();
            if (entity != null) {
                if (entity != mc.player) {
                    final GameProfile profile = new GameProfile(mc.player.getUniqueID(), "");
                    (player = new EntityOtherPlayerMP(mc.world, profile)).copyLocationAndAnglesFrom(entity);
                    player.rotationYaw = entity.rotationYaw;
                    player.rotationYawHead = entity.rotationYawHead;
                    player.rotationPitch = entity.rotationPitch;
                    player.prevRotationPitch = entity.prevRotationPitch;
                    player.prevRotationYaw = entity.prevRotationYaw;
                    player.renderYawOffset = entity.renderYawOffset;
                    this.startTime = System.currentTimeMillis();
                }
            }
        }
    }



    public void onRender3D(Render3DEvent eventRender3D) {
        block6: {
            if (mc.player == null || mc.world == null) {
                return;
            }
            color = new Color(130, 0, 255, 195);
            outlineColor =  new Color(130, 0, 255, 255);
            opacity = Float.intBitsToFloat(Float.floatToIntBits(1.6358529E38f) ^ 0x7EF622C3);
            time = System.currentTimeMillis();
            duration = time - this.startTime;
            startAlpha = (float)195 / Float.intBitsToFloat(Float.floatToIntBits(0.0119778095f) ^ 0x7F3B3E93);
            if (player == null || entity == null) break block6;
            if (duration < (long)(2500 * 10)) {
                opacity = startAlpha - (float)duration / (float)(2500 * 10);
            }
            if (duration < (long)(200 * 10)) {
                GL11.glPushMatrix();
                    GlStateManager.translate(Float.intBitsToFloat(Float.floatToIntBits(1.240196E38f) ^ 0x7EBA9A9D), (float)duration / (float)(150 * 10), Float.intBitsToFloat(Float.floatToIntBits(3.0414126E38f) ^ 0x7F64CF7A));
                }
                mc.renderManager.renderEntityStatic(player, Float.intBitsToFloat(Float.floatToIntBits(6.159893f) ^ 0x7F451DD8), false);
                GlStateManager.translate(Float.intBitsToFloat(Float.floatToIntBits(3.0715237E38f) ^ 0x7F671365), Float.intBitsToFloat(Float.floatToIntBits(1.9152719E37f) ^ 0x7D668ADF), Float.intBitsToFloat(Float.floatToIntBits(1.9703683E38f) ^ 0x7F143BEA));
                GL11.glPopMatrix();
            }
        }
    }
