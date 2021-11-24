package me.derp.quantum.features.modules.player;

import me.derp.quantum.event.events.BlockEvent;
import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.event.events.Render3DEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.BlockUtil;
import me.derp.quantum.util.RenderUtil;
import me.derp.quantum.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public
class InstantMine extends Module {
    private final Timer breakTimer;
    public Setting <Integer> delay = this.register(new Setting <Object>("Delay", 65, 0, 500));
    public Setting <Boolean> picOnly = this.register(new Setting <Object>("PicOnly", true));
    public Setting <Boolean> render = this.register(new Setting <>("Render", false));
    public Setting <Boolean> box = this.register(new Setting <Object>("Box", true, v -> this.render.getValue()));
    private final Setting <Integer> boxAlpha = this.register(new Setting <Object>("BoxAlpha", 85, 0, 255, v -> this.box.getValue() && this.render.getValue()));
    public Setting <Integer> red = this.register(new Setting <Object>("Red", 125, 0, 255, v -> this.render.getValue()));
    public Setting <Integer> green = this.register(new Setting <Object>("Green", 0, 0, 255, v -> this.render.getValue()));
    public Setting <Integer> blue = this.register(new Setting <Object>("Blue", 255, 0, 255, v -> this.render.getValue()));
    public Setting <Boolean> outline = this.register(new Setting <Object>("Outline", true, v -> this.render.getValue()));
    public final Setting <Float> lineWidth = this.register(new Setting <Object>("LineWidth", 1.0f, 0.1f, 5.0f, v -> this.outline.getValue() && this.render.getValue()));
    private BlockPos renderBlock;
    private BlockPos lastBlock;
    private boolean packetCancel;
    private EnumFacing direction;

    public
    InstantMine() {
        super("InstantMine", "Instantly mine blocks placed in the same spot.", Category.PLAYER, true, false, false);
        this.packetCancel = false;
        this.breakTimer = new Timer();
    }

    @Override
    public
    void onRender3D(final Render3DEvent event) {
        if (this.render.getValue()) {
            if (this.renderBlock != null) {
                final Color color = new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.boxAlpha.getValue());
                RenderUtil.drawBoxESP(this.renderBlock, color, false, color, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), false);
            }
        }
    }

    @SubscribeEvent
    public
    void onPacketSend(final PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerDigging) {
            final CPacketPlayerDigging digPacket = event.getPacket();
            if (digPacket.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK && this.packetCancel) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public
    void onTick() {
        if (this.renderBlock == null || !this.breakTimer.passedMs(this.delay.getValue())) {
            try {
                InstantMine.mc.playerController.blockHitDelay = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        this.breakTimer.reset();
        if (this.picOnly.getValue() && InstantMine.mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.DIAMOND_PICKAXE) {
            return;
        }
        InstantMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.renderBlock, this.direction));
    }

    @SubscribeEvent
    public
    void onBlockEvent(final BlockEvent event) {
        if (InstantMine.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 3 && InstantMine.mc.playerController.curBlockDamageMP > 0.1f) {
            InstantMine.mc.playerController.isHittingBlock = true;
        }
        if (event.getStage() == 4 && BlockUtil.canBreak(event.pos)) {
            InstantMine.mc.playerController.isHittingBlock = false;
            if (this.canBreak(event.pos)) {
                if (this.lastBlock == null || event.pos.getX() != this.lastBlock.getX() || event.pos.getY() != this.lastBlock.getY() || event.pos.getZ() != this.lastBlock.getZ()) {
                    this.packetCancel = false;
                    InstantMine.mc.player.swingArm(EnumHand.MAIN_HAND);
                    InstantMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.pos, event.facing));
                    this.packetCancel = true;
                } else {
                    this.packetCancel = true;
                }
                InstantMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                this.renderBlock = event.pos;
                this.lastBlock = event.pos;
                this.direction = event.facing;
                event.setCanceled(true);
            }
        }
    }

    private
    boolean canBreak(final BlockPos pos) {
        final IBlockState blockState = InstantMine.mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, InstantMine.mc.world, pos) != -1.0f;
    }
}