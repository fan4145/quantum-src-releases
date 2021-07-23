package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.util.PairUtil;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class NewChunks
        extends Module {

    public NewChunks() {
        super("NewChunks", "newchunks", Module.Category.RENDER, false, false, false);
    }

    private List<PairUtil<Integer, Integer>> chunkDataList = new CopyOnWriteArrayList<>();


    @Override
    public void onRender3D(Render3DEvent event) {
        chunkDataList.forEach(chunkData ->
        {
            float x = chunkData.getKey() * 16;
            float z = chunkData.getValue() * 16;

            RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            final AxisAlignedBB bb = new AxisAlignedBB(
                    x - mc.getRenderManager().viewerPosX,
                    0 - mc.getRenderManager().viewerPosY,
                    z - mc.getRenderManager().viewerPosZ,
                    x + 16 - mc.getRenderManager().viewerPosX,
                    1 - mc.getRenderManager().viewerPosY,
                    z + 16 - mc.getRenderManager().viewerPosZ);

            final AxisAlignedBB t = new AxisAlignedBB(x, 0, z, x + 16, 1, z + 16);
            if (RenderUtil.camera.isBoundingBoxInFrustum(t)) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glLineWidth(1.5f);

                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, 0.6f, 0f, 0.9333333f, 0.5f);

                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        });
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketChunkData) { final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
        if (!packet.isFullChunk()) {
            chunkDataList.removeIf(chunkData ->
            {
                return chunkData.getKey() == packet.getChunkX() && chunkData.getValue() == packet.getChunkZ();
            });

            final PairUtil<Integer, Integer> chunk = new PairUtil<>(packet.getChunkX(), packet.getChunkZ());
            if (!this.chunkDataList.contains(chunk)) {
                this.chunkDataList.add(chunk);
            }
        }
    }
}}





