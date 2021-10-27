package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.events.DamageBlockEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.RenderUtil;
import me.alpha432.oyvey.util.Timer;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class InstantMine extends Module {

    private BlockPos renderBlock;
    private BlockPos lastBlock;
    private boolean packetCancel = false;
    private final Timer breaktimer = new Timer();
    private EnumFacing direction;

    private final Setting<Boolean> autoBreak = this.register(new Setting<Boolean>("AutoBreak", true));
    private final Setting<Integer> delay = this.register(new Setting<Integer>("Delay", 250, 1, 500));
    private final Setting<Boolean> picOnly = this.register(new Setting<Boolean>("PickaxeOnly", false));

    public static InstantMine INSTANCE;

    public InstantMine() {
        super("InstantMine", "funny exploit lol :^)", Category.PLAYER, true, false, false);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
    }

    public static InstantMine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InstantMine();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (renderBlock != null) {
            RenderUtil.drawBox(renderBlock, new Color(255, 255, 255));
        }
    }

    @Override
    public void onUpdate() {
        if (renderBlock != null) {
            if (autoBreak.getValue() && breaktimer.passed(delay.getValue())) {
                if (picOnly.getValue() && !(mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.DIAMOND_PICKAXE))
                    return;
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        renderBlock, direction));
                breaktimer.reset();
            }

        }

        try {
            mc.playerController.blockHitDelay = 0;

        } catch (Exception e) {
        }
    }

    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {
        Packet packet = event.getPacket();
        if (packet instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging digPacket = (CPacketPlayerDigging) packet;
            if (((CPacketPlayerDigging) packet).getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK && packetCancel)
                event.setCanceled(true);
        }
    });

    @EventHandler
    private final Listener<DamageBlockEvent> OnDamageBlock = new Listener<>(p_Event -> {
        if (canBreak(p_Event.getPos())) {

            if (lastBlock == null || p_Event.getPos().getX() != lastBlock.getX() || p_Event.getPos().getY() != lastBlock.getY() || p_Event.getPos().getZ() != lastBlock.getZ()) {
                //Command.sendChatMessage("New Block");
                packetCancel = false;
                //Command.sendChatMessage(p_Event.getPos()+" : "+lastBlock);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                        p_Event.getPos(), p_Event.getDirection()));
                packetCancel = true;
            } else {
                packetCancel = true;
            }
            //Command.sendChatMessage("Breaking");
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                    p_Event.getPos(), p_Event.getDirection()));

            renderBlock = p_Event.getPos();
            lastBlock = p_Event.getPos();
            direction = p_Event.getDirection();

            p_Event.cancel();

        }
    });

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, mc.world, pos) != -1;
    }

    public BlockPos getTarget() {
        return renderBlock;
    }

    public void setTarget(BlockPos pos) {
        renderBlock = pos;
        packetCancel = false;
        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                pos, EnumFacing.DOWN));
        packetCancel = true;
        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                pos, EnumFacing.DOWN));
        direction = EnumFacing.DOWN;
        lastBlock = pos;
    }

}