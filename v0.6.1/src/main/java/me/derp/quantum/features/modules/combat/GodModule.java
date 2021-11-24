package me.derp.quantum.features.modules.combat;


import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.Feature;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.BlockUtil;
import me.derp.quantum.util.MathUtil;
import me.derp.quantum.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.TimeUnit;

public class GodModule
        extends Module {
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private boolean rotating;
    private int rotationPacketsSpoofed;
    private int highestID = -100000;
    public Setting<Integer> rotations = this.register(new Setting<>("Spoofs", 1, 1, 20));
    public Setting<Boolean> rotate = this.register(new Setting<>("Rotate", false));
    public Setting<Boolean> render = this.register(new Setting<>("Render", false));
    public Setting<Boolean> antiIllegal = this.register(new Setting<>("AntiIllegal", true));
    public Setting<Boolean> checkPos = this.register(new Setting<>("CheckPos", false));
    public Setting<Boolean> oneDot15 = this.register(new Setting<>("1.15", false));
    public Setting<Boolean> entitycheck = this.register(new Setting<>("EntityCheck", false));
    public Setting<Integer> attacks = this.register(new Setting<>("Attacks", 1, 1, 10));
    public Setting<Integer> offset = this.register(new Setting<>("Offset", 0, 0, 2));
    public Setting<Integer> delay = this.register(new Setting<>("Delay", 0, 0, 250));

    public GodModule() {
        super("GodModule", "Wow", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onToggle() {
        this.resetFields();
        if (GodModule.mc.world != null) {
            this.updateEntityID();
        }
    }

    @Override
    public void onUpdate() {
        if (this.render.getValue()) {
            for (Entity entity : GodModule.mc.world.loadedEntityList) {
                if (!(entity instanceof EntityEnderCrystal)) continue;
                entity.setCustomNameTag(String.valueOf(entity.entityId));
                entity.setAlwaysRenderNameTag(true);
            }
        }
    }

    @Override
    public void onLogout() {
        this.resetFields();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSendPacket(PacketEvent.Send event) {
        CPacketPlayerTryUseItemOnBlock packet;
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            packet = event.getPacket();
            if (GodModule.mc.player.getHeldItem(packet.hand).getItem() instanceof ItemEndCrystal) {
                if (this.checkPos.getValue() && !BlockUtil.canPlaceCrystal(packet.position, this.entitycheck.getValue(), this.oneDot15.getValue()) || this.checkPlayers()) {
                    return;
                }
                this.updateEntityID();
                for (int i = 1 - this.offset.getValue(); i <= this.attacks.getValue(); ++i) {
                    this.attackID(packet.position, this.highestID + i);
                }
            }
        }
        if (event.getStage() == 0 && this.rotating && this.rotate.getValue() && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet2 = event.getPacket();
            packet2.yaw = this.yaw;
            packet2.pitch = this.pitch;
            ++this.rotationPacketsSpoofed;
            if (this.rotationPacketsSpoofed >= this.rotations.getValue()) {
                this.rotating = false;
                this.rotationPacketsSpoofed = 0;
            }
        }
    }

    private void attackID(BlockPos pos, int id) {
        Entity entity = GodModule.mc.world.getEntityByID(id);
        if (entity == null || entity instanceof EntityEnderCrystal) {
            AttackThread attackThread = new AttackThread(id, pos, this.delay.getValue(), this);
            if (this.delay.getValue() == 0) {
                attackThread.start();
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSpawnObject) {
            this.checkID(((SPacketSpawnObject) event.getPacket()).getEntityID());
        } else if (event.getPacket() instanceof SPacketSpawnExperienceOrb) {
            this.checkID(((SPacketSpawnExperienceOrb) event.getPacket()).getEntityID());
        } else if (event.getPacket() instanceof SPacketSpawnPlayer) {
            this.checkID(((SPacketSpawnPlayer) event.getPacket()).getEntityID());
        } else if (event.getPacket() instanceof SPacketSpawnGlobalEntity) {
            this.checkID(((SPacketSpawnGlobalEntity) event.getPacket()).getEntityId());
        } else if (event.getPacket() instanceof SPacketSpawnPainting) {
            this.checkID(((SPacketSpawnPainting) event.getPacket()).getEntityID());
        } else if (event.getPacket() instanceof SPacketSpawnMob) {
            this.checkID(((SPacketSpawnMob) event.getPacket()).getEntityID());
        }
    }

    private void checkID(int id) {
        if (id > this.highestID) {
            this.highestID = id;
        }
    }

    public void updateEntityID() {
        for (Entity entity : GodModule.mc.world.loadedEntityList) {
            if (entity.getEntityId() <= this.highestID) continue;
            this.highestID = entity.getEntityId();
        }
    }

    private boolean checkPlayers() {
        if (this.antiIllegal.getValue()) {
            for (EntityPlayer player : GodModule.mc.world.playerEntities) {
                if (this.checkItem(player.getHeldItemMainhand()) && this.checkItem(player.getHeldItemOffhand()))
                    continue;
                return true;
            }
        }
        return false;
    }

    private boolean checkItem(ItemStack stack) {
        return !(stack.getItem() instanceof ItemBow) && !(stack.getItem() instanceof ItemExpBottle) && stack.getItem() != Items.STRING;
    }

    public void rotateTo(BlockPos pos) {
        float[] angle = MathUtil.calcAngle(GodModule.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos));
        this.yaw = angle[0];
        this.pitch = angle[1];
        this.rotating = true;
    }

    private void resetFields() {
        this.rotating = false;
        this.highestID = -1000000;
    }

    public static class AttackThread
            extends Thread {
        private final BlockPos pos;
        private final int id;
        private final int delay;
        private final GodModule godModule;

        public AttackThread(int idIn, BlockPos posIn, int delayIn, GodModule godModuleIn) {
            this.id = idIn;
            this.pos = posIn;
            this.delay = delayIn;
            this.godModule = godModuleIn;
        }

        @Override
        public void run() {
            try {
                if (this.delay != 0) {
                    TimeUnit.MILLISECONDS.sleep(this.delay);
                }
                Util.mc.addScheduledTask(() -> {
                    if (!Feature.fullNullCheck()) {
                        CPacketUseEntity attack = new CPacketUseEntity();
                        attack.entityId = this.id;
                        attack.action = CPacketUseEntity.Action.ATTACK;
                        this.godModule.rotateTo(this.pos.up());
                        Util.mc.player.connection.sendPacket(attack);
                        Util.mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}