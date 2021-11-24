package me.derp.quantum.features.modules.combat;

import me.derp.quantum.Quantum;
import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.event.events.Render3DEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.modules.client.ClickGui;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.BlockUtil;
import me.derp.quantum.util.ColorUtil;
import me.derp.quantum.util.EntityUtil;
import me.derp.quantum.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HoleFiller extends Module {
    private final Setting<Double> range = register(new Setting<>("Range", 4.5, 0.1, 6.0));
    private final Setting<Boolean> smart = (Setting<Boolean>) this.register(new Setting<>("Smart", false));
    private final Setting<Double> smartRange = (Setting<Double>) this.register(new Setting<>("Smart Range", 4.0, 0.1, 6.0));
    private final Setting<Boolean> rainbow = register(new Setting<>("Rainbow", false));
    private final Setting<Integer> red = register(new Setting("Red", 0, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> green = register(new Setting("Green", 255, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 0, 0, 255, v -> !this.rainbow.getValue()));
    private final Setting<Integer> outlineAlpha = register(new Setting("OL-Alpha", 0, 0, 255, v -> !this.rainbow.getValue()));
    private BlockPos render;
    private EntityPlayer closestTarget;
    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;
    private static HoleFiller INSTANCE;

    public HoleFiller() {
        super("HoleFiller", "Fills holes around you.", Category.COMBAT, true, false, true);
        this.setInstance();

    }

    public static HoleFiller getInstance() {
        if (HoleFiller.INSTANCE == null) {
            HoleFiller.INSTANCE = new HoleFiller();
        }
        return HoleFiller.INSTANCE;
    }

    private void setInstance() {
        HoleFiller.INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        final Packet<? extends net.minecraft.network.INetHandler> packet = event.getPacket();
        if (packet instanceof CPacketPlayer && HoleFiller.isSpoofingAngles) {
            ((CPacketPlayer) packet).yaw = (float) HoleFiller.yaw;
            ((CPacketPlayer) packet).pitch = (float) HoleFiller.pitch;
        }
    }

    @Override
    public void onUpdate() {
        if (HoleFiller.mc.world == null) {
            return;
        }
        if (this.smart.getValue()) {
            this.findClosestTarget();
        }
        List<BlockPos> blocks = findCrystalBlocks();
        BlockPos q = null;
        int obsidianSlot = ((HoleFiller.mc.player.getHeldItemMainhand().getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN)) ? HoleFiller.mc.player.inventory.currentItem : -1);
        if (obsidianSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (HoleFiller.mc.player.inventory.getStackInSlot(l).getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN)) {
                    obsidianSlot = l;
                    break;
                }
            }
        }
        if (obsidianSlot == -1) {
            return;
        }
        for (final BlockPos blockPos : blocks) {
            if (!HoleFiller.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty()) {
                continue;
            }
            if (this.smart.getValue() && this.isInRange(blockPos)) {
                q = blockPos;
            } else {
                q = blockPos;
            }
        }
        this.render = q;
        if (q != null && HoleFiller.mc.player.onGround) {
            final int oldSlot = HoleFiller.mc.player.inventory.currentItem;
            if (HoleFiller.mc.player.inventory.currentItem != obsidianSlot) {
                HoleFiller.mc.player.inventory.currentItem = obsidianSlot;
            }
            this.lookAtPacket(q.getX() + 0.5, q.getY() - 0.5, q.getZ() + 0.5, HoleFiller.mc.player);
            BlockUtil.placeBlockScaffold(this.render);
            HoleFiller.mc.player.swingArm(EnumHand.MAIN_HAND);
            HoleFiller.mc.player.inventory.currentItem = oldSlot;
            resetRotation();
        }
    }

    @Override
    public void onRender3D(final Render3DEvent event) {
        if (this.render != null) {
            RenderUtil.drawBoxESP(this.render, rainbow.getValue() ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(red.getValue(), green.getValue(), blue.getValue(), outlineAlpha.getValue()), 3.5F, true, true, alpha.getValue());
        }
    }

    private void lookAtPacket(final double px, final double py, final double pz, final EntityPlayer me) {
        final double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1]);
    }

    public boolean IsHole(final BlockPos blockPos) {
        final BlockPos boost = blockPos.add(0, 1, 0);
        final BlockPos boost2 = blockPos.add(0, 0, 0);
        final BlockPos boost3 = blockPos.add(0, 0, -1);
        final BlockPos boost4 = blockPos.add(1, 0, 0);
        final BlockPos boost5 = blockPos.add(-1, 0, 0);
        final BlockPos boost6 = blockPos.add(0, 0, 1);
        final BlockPos boost7 = blockPos.add(0, 2, 0);
        final BlockPos boost8 = blockPos.add(0.5, 0.5, 0.5);
        final BlockPos boost9 = blockPos.add(0, -1, 0);
        return HoleFiller.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && HoleFiller.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && HoleFiller.mc.world.getBlockState(boost7).getBlock() == Blocks.AIR && (HoleFiller.mc.world.getBlockState(boost3).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost3).getBlock() == Blocks.BEDROCK) && (HoleFiller.mc.world.getBlockState(boost4).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost4).getBlock() == Blocks.BEDROCK) && (HoleFiller.mc.world.getBlockState(boost5).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost5).getBlock() == Blocks.BEDROCK) && (HoleFiller.mc.world.getBlockState(boost6).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost6).getBlock() == Blocks.BEDROCK) && HoleFiller.mc.world.getBlockState(boost8).getBlock() == Blocks.AIR && (HoleFiller.mc.world.getBlockState(boost9).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost9).getBlock() == Blocks.BEDROCK);
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(HoleFiller.mc.player.posX), Math.floor(HoleFiller.mc.player.posY), Math.floor(HoleFiller.mc.player.posZ));
    }

    public BlockPos getClosestTargetPos() {
        if (this.closestTarget != null) {
            return new BlockPos(Math.floor(this.closestTarget.posX), Math.floor(this.closestTarget.posY), Math.floor(this.closestTarget.posZ));
        }
        return null;
    }

    private void findClosestTarget() {
        final List<EntityPlayer> playerList = HoleFiller.mc.world.playerEntities;
        this.closestTarget = null;
        for (final EntityPlayer target : playerList) {
            if (target != HoleFiller.mc.player && !Quantum.friendManager.isFriend(target.getName()) && EntityUtil.isLiving(target)) {
                if (target.getHealth() <= 0.0f) {
                    continue;
                }
                if (this.closestTarget == null) {
                    this.closestTarget = target;
                } else {
                    if (HoleFiller.mc.player.getDistance(target) >= HoleFiller.mc.player.getDistance(this.closestTarget)) {
                        continue;
                    }
                    this.closestTarget = target;
                }
            }
        }
    }

    private boolean isInRange(BlockPos blockPos) {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(
                getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0)
                        .stream().filter(this::IsHole).collect(Collectors.toList()));
        return positions.contains(blockPos);
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList<BlockPos> positions = NonNullList.create();
        if (smart.getValue() && closestTarget != null)
            positions.addAll(
                    getSphere(getClosestTargetPos(), smartRange.getValue().floatValue(), range.getValue().intValue(), false, true, 0)
                            .stream().filter(this::IsHole).filter(this::isInRange).collect(Collectors.toList()));
        else if (!smart.getValue())
            positions.addAll(
                    getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0)
                            .stream().filter(this::IsHole).collect(Collectors.toList()));
        return positions;
    }

    public List<BlockPos> getSphere(final BlockPos loc, final float r, final int h, final boolean hollow, final boolean sphere, final int plus_y) {
        final ArrayList<BlockPos> circleblocks = new ArrayList<>();
        final int cx = loc.getX();
        final int cy = loc.getY();
        final int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; ++x) {
            for (int z = cz - (int) r; z <= cz + r; ++z) {
                int y = sphere ? (cy - (int) r) : cy;
                while (true) {
                    final float f = (float) y;
                    final float f2 = sphere ? (cy + r) : ((float) (cy + h));
                    if (f >= f2) {
                        break;
                    }
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? ((cy - y) * (cy - y)) : 0);
                    if (dist < r * r && (!hollow || dist >= (r - 1.0f) * (r - 1.0f))) {
                        final BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                }
            }
        }
        return circleblocks;
    }

    private static void setYawAndPitch(final float yaw1, final float pitch1) {
        HoleFiller.yaw = yaw1;
        HoleFiller.pitch = pitch1;
        HoleFiller.isSpoofingAngles = true;
    }

    private static void resetRotation() {
        if (HoleFiller.isSpoofingAngles) {
            HoleFiller.yaw = HoleFiller.mc.player.rotationYaw;
            HoleFiller.pitch = HoleFiller.mc.player.rotationPitch;
            HoleFiller.isSpoofingAngles = false;
        }
    }

    @Override
    public void onDisable() {
        this.closestTarget = null;
        this.render = null;
        resetRotation();
        super.onDisable();
    }

    static {
        HoleFiller.INSTANCE = new HoleFiller();
    }
}