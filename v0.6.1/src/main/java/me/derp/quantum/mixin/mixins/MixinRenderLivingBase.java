package me.derp.quantum.mixin.mixins;

import me.derp.quantum.Quantum;
import me.derp.quantum.event.events.RenderEntityModelEvent;
import me.derp.quantum.features.modules.client.ClickGui;
import me.derp.quantum.features.modules.render.Wireframe;
import me.derp.quantum.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={RenderLivingBase.class})
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {

    @Shadow
    protected abstract void renderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn);

    @Shadow
    protected abstract void unsetBrightness();

    @Shadow
    protected abstract boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks);

    @Shadow
    protected ModelBase mainModel;

    @Shadow
    protected abstract float getSwingProgress(T livingBase, float partialTickTime);

    @Shadow
    protected abstract float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks);

    @Shadow
    protected abstract void renderLivingAt(T entityLivingBaseIn, double x, double y, double z);

    @Shadow
    protected abstract float handleRotationFloat(T livingBase, float partialTicks);

    @Shadow
    protected abstract void applyRotations(T entityLiving, float ageInTicks, float rotationYaw, float partialTicks);

    @Shadow
    public abstract float prepareScale(T entitylivingbaseIn, float partialTicks);

    @Shadow
    protected abstract boolean setScoreTeamColor(T entityLivingBaseIn);

    @Shadow
    protected boolean renderMarker;

    @Shadow
    protected abstract void unsetScoreTeamColor();

    @Shadow
    protected abstract void renderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor);

    float red;

    float green;

    float blue;

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
        this.red = 0.0F;
        this.green = 0.0F;
        this.blue = 0.0F;


    }


    private static final ResourceLocation glint = new ResourceLocation("textures/shinechams.png");

    public MixinRenderLivingBase(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
    }


    /**
     * @author d
     */
    @Overwrite
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre(entity, RenderLivingBase.class.cast(this), partialTicks, x, y, z))) {
            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            this.mainModel.swingProgress = getSwingProgress(entity, partialTicks);
            boolean shouldSit = (entity.isRiding() && entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
            this.mainModel.isRiding = shouldSit;
            this.mainModel.isChild = entity.isChild();
            try {
                float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
                float f1 = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
                float f2 = f1 - f;
                if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
                    f = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    f2 = f1 - f;
                    float f3 = MathHelper.wrapDegrees(f2);
                    if (f3 < -85.0F)
                        f3 = -85.0F;
                    if (f3 >= 85.0F)
                        f3 = 85.0F;
                    f = f1 - f3;
                    if (f3 * f3 > 2500.0F)
                        f += f3 * 0.2F;
                    f2 = f1 - f;
                }
                float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                renderLivingAt(entity, x, y, z);
                float f8 = handleRotationFloat(entity, partialTicks);
                applyRotations(entity, f8, f, partialTicks);
                float f4 = prepareScale(entity, partialTicks);
                float f5 = 0.0F;
                float f6 = 0.0F;
                if (!entity.isRiding()) {
                    f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                    f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
                    if (entity.isChild())
                        f6 *= 3.0F;
                    if (f5 > 1.0F)
                        f5 = 1.0F;
                    f2 = f1 - f;
                }
                GlStateManager.enableAlpha();
                this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
                this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, f4, entity);
                if (this.renderOutlines) {
                    boolean flag1 = setScoreTeamColor(entity);
                    GlStateManager.enableColorMaterial();
                    GlStateManager.enableOutlineMode(getTeamColor(entity));
                    if (!this.renderMarker)
                        renderModel(entity, f6, f5, f8, f2, f7, f4);
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator())
                        renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
                    GlStateManager.disableOutlineMode();
                    GlStateManager.disableColorMaterial();
                    if (flag1)
                        unsetScoreTeamColor();
                } else {
                    if (Wireframe.getINSTANCE().isOn() && (Wireframe.getINSTANCE()).players.getValue() && entity instanceof EntityPlayer && (Wireframe.getINSTANCE()).mode.getValue().equals(Wireframe.RenderMode.SOLID)) {
                        this.red = (ClickGui.getInstance()).red.getValue() / 255.0F;
                        this.green = (ClickGui.getInstance()).green.getValue() / 255.0F;
                        this.blue = (ClickGui.getInstance()).blue.getValue() / 255.0F;
                        GlStateManager.pushMatrix();
                        GL11.glPushAttrib(1048575);
                        GL11.glDisable(3553);
                        GL11.glDisable(2896);
                        GL11.glEnable(2848);
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glDisable(2929);
                        GL11.glDepthMask(false);
                        if (Quantum.friendManager.isFriend(entity.getName()) || entity == (Minecraft.getMinecraft()).player) {
                            GL11.glColor4f(0.0F, 191.0F, 255.0F, (Wireframe.getINSTANCE()).alpha.getValue() / 255.0F);
                        } else {
                            GL11.glColor4f((ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRed() / 255.0F) : this.red, (ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getGreen() / 255.0F) : this.green, (ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getBlue() / 255.0F) : this.blue, (Wireframe.getINSTANCE()).alpha.getValue() / 255.0F);
                        }
                        renderModel(entity, f6, f5, f8, f2, f7, f4);
                        GL11.glDisable(2896);
                        GL11.glEnable(2929);
                        GL11.glDepthMask(true);
                        if (Quantum.friendManager.isFriend(entity.getName()) || entity == (Minecraft.getMinecraft()).player) {
                            GL11.glColor4f(0.0F, 191.0F, 255.0F, (Wireframe.getINSTANCE()).alpha.getValue() / 255.0F);
                        } else {
                            GL11.glColor4f((ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRed() / 255.0F) : this.red, (ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getGreen() / 255.0F) : this.green, (ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getBlue() / 255.0F) : this.blue, (Wireframe.getINSTANCE()).alpha.getValue() / 255.0F);
                        }
                        renderModel(entity, f6, f5, f8, f2, f7, f4);
                        GL11.glEnable(2896);
                        GlStateManager.popAttrib();
                        GlStateManager.popMatrix();
                    }
                    boolean flag1 = setDoRenderBrightness(entity, partialTicks);
                    if (!(entity instanceof EntityPlayer) || (Wireframe.getINSTANCE().isOn() && (Wireframe.getINSTANCE()).mode.getValue().equals(Wireframe.RenderMode.WIREFRAME) && (Wireframe.getINSTANCE()).playerModel.getValue()) || Wireframe.getINSTANCE().isOff())
                        renderModel(entity, f6, f5, f8, f2, f7, f4);
                    if (flag1)
                        unsetBrightness();
                    GlStateManager.depthMask(true);
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator())
                        renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
                    if (Wireframe.getINSTANCE().isOn() && (Wireframe.getINSTANCE()).players.getValue() && entity instanceof EntityPlayer && (Wireframe.getINSTANCE()).mode.getValue().equals(Wireframe.RenderMode.WIREFRAME)) {
                        this.red = (ClickGui.getInstance()).red.getValue() / 255.0F;
                        this.green = (ClickGui.getInstance()).green.getValue() / 255.0F;
                        this.blue = (ClickGui.getInstance()).blue.getValue() / 255.0F;
                        GlStateManager.pushMatrix();
                        GL11.glPushAttrib(1048575);
                        GL11.glPolygonMode(1032, 6913);
                        GL11.glDisable(3553);
                        GL11.glDisable(2896);
                        GL11.glDisable(2929);
                        GL11.glEnable(2848);
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        if (Quantum.friendManager.isFriend(entity.getName()) || entity == (Minecraft.getMinecraft()).player) {
                            GL11.glColor4f(0.0F, 191.0F, 255.0F, (Wireframe.getINSTANCE()).alpha.getValue() / 255.0F);
                        } else {
                            GL11.glColor4f((ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRed() / 255.0F) : this.red, (ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getGreen() / 255.0F) : this.green, (ClickGui.getInstance()).rainbow.getValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getBlue() / 255.0F) : this.blue, (Wireframe.getINSTANCE()).alpha.getValue() / 255.0F);
                        }
                        GL11.glLineWidth((Wireframe.getINSTANCE()).lineWidth.getValue());
                        renderModel(entity, f6, f5, f8, f2, f7, f4);
                        GL11.glEnable(2896);
                        GlStateManager.popAttrib();
                        GlStateManager.popMatrix();
                    }
                }
                GlStateManager.disableRescaleNormal();
            } catch (Exception var20) {
                Quantum.LOGGER.error("Couldn't render entity", var20);
            }
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
            MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(entity, RenderLivingBase.class.cast(this), partialTicks, x, y, z));
        }
    }
}


