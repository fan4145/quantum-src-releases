package me.derp.quantum.features.modules.combat;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.*;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Surround extends Module {

    private final Setting<Integer> delay = this.register(new Setting<>("Delay", 50, 0, 250));
    private final Setting<Integer> blocksPerTick = this.register(new Setting<>("BPT", 8, 1, 20));
    private final Setting<Boolean> helpingBlocks = this.register(new Setting<>("HelpingBlocks", true));
    private final Setting<Boolean> intelligent = this.register(new Setting<>("Intelligent", false));
    private final Setting<Boolean> antiPedo = this.register(new Setting<>("Always Help", false));
    private final Setting<Boolean> floor = this.register(new Setting<>("Floor", false));
    private final Setting<Integer> retryer = this.register(new Setting<>("Retries", 4, 1, 15));
    private final Setting<Integer> retryDelay = this.register(new Setting<>("Retry Delay", 200, 1, 2500));

    private final Map<BlockPos, Integer> retries = new HashMap<>();
    private final Timer timer = new Timer();
    private final Timer retryTimer = new Timer();

    private boolean didPlace = false;
    private int placements = 0;
    private int obbySlot = -1;

    double posY;

    public Surround() {
        super("SurroundRewrite", "Surrounds you with obsidian", Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            setEnabled(false);
            return;
        }
        retries.clear();
        retryTimer.reset();
        posY = mc.player.posY;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (check()) {
            return;
        }

        if (posY < mc.player.posY) {
            setEnabled(false);
            return;
        }

        boolean onEChest = mc.world.getBlockState(new BlockPos(mc.player.getPositionVector())).getBlock() == Blocks.ENDER_CHEST;
        if (mc.player.posY - (int)mc.player.posY < 0.7) {
            onEChest = false;
        }
        if (!isSafe(mc.player, onEChest ? 1:0, floor.getValue())) {
            placeBlocks(mc.player.getPositionVector(), getUnsafeBlockArray(mc.player, onEChest ? 1 : 0, floor.getValue()), helpingBlocks.getValue(), false);
        } else if (!isSafe(mc.player, onEChest ? 0 : -1, false)) {
            if (antiPedo.getValue()) {
                placeBlocks(mc.player.getPositionVector(), getUnsafeBlockArray(mc.player, onEChest ? 0 : -1, false), false, false);
            }
        }

        if (didPlace) {
            timer.reset();
        }
    }

    public static Vec3d[] getUnsafeBlockArray(Entity entity, int height, boolean floor) {
        final List<Vec3d> list = getUnsafeBlocks(entity, height, floor);
        final Vec3d[] array = new Vec3d[list.size()];
        return list.toArray(array);
    }

    public static boolean isSafe(Entity entity, int height, boolean floor) {
        return getUnsafeBlocks(entity, height, floor).size() == 0;
    }

    public static List<Vec3d> getUnsafeBlocks(Entity entity, int height, boolean floor) {
        return getUnsafeBlocksFromVec3d(entity.getPositionVector(), height, floor);
    }

    public static List<Vec3d> getUnsafeBlocksFromVec3d(Vec3d pos, int height, boolean floor) {
        final List<Vec3d> vec3ds = new ArrayList<>(floor ? 5 : 4);
        for (final Vec3d vector : getOffsets(height, floor)) {
            final Block block = mc.world.getBlockState(new BlockPos(pos).add(vector.x, vector.y, vector.z)).getBlock();
            if (block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow) {
                vec3ds.add(vector);
            }
        }
        return vec3ds;
    }

    public static Vec3d[] getOffsets(int y, boolean floor) {
        final List<Vec3d> offsets = getOffsetList(y, floor);
        final Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }

    public static List<Vec3d> getOffsetList(int y, boolean floor) {
        final List<Vec3d> offsets = new ArrayList<>(floor ? 5 : 4);
        offsets.add(new Vec3d(-1, y, 0));
        offsets.add(new Vec3d(1, y, 0));
        offsets.add(new Vec3d(0, y, -1));
        offsets.add(new Vec3d(0, y, 1));

        if (floor) {
            offsets.add(new Vec3d(0, y - 1, 0));
        }

        return offsets;
    }

    private boolean placeBlocks(Vec3d pos, Vec3d[] vec3ds, boolean hasHelpingBlocks, boolean isHelping) {
        int helpings = 0;
        boolean gotHelp;
        if (obbySlot == -1)
            return false;

        if (mc.player == null)
            return false;

        boolean switched = false;
        int lastSlot = mc.player.inventory.currentItem;
        for (final Vec3d vec3d : vec3ds) {
            if (!switched) {
                if (mc.player.inventory.currentItem != obbySlot) {
                    InventoryUtil.switchToHotbarSlot(obbySlot, false);
                }
                switched = true;
            }
            gotHelp = true;
            helpings++;
            if (isHelping && !intelligent.getValue() && helpings > 1) {
                return false;
            }
            final BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
            switch (BlockUtil.isPositionPlaceable(position, true)) {
                case 1:
                    if ((this.retries.get(position) == null || this.retries.get(position) < this.retryer.getValue())) {
                        placeBlock(position);
                        this.retries.put(position, this.retries.get(position) == null ? 1 : this.retries.get(position) + 1);
                        this.retryTimer.reset();
                        continue;
                    }

                    continue;
                case 2:
                    if (hasHelpingBlocks) {
                        gotHelp = placeBlocks(pos, BlockUtil.getHelpingBlocks(vec3d), false, true);
                    } else {
                        continue;
                    }
                case 3:
                    if (gotHelp) {
                        placeBlock(position);
                    }
                    if (isHelping) {
                        return true;
                    }
            }
        }
        if (switched && mc.player.inventory.currentItem != lastSlot) {
            InventoryUtil.switchToHotbarSlot(lastSlot, false);
        }
        return false;
    }
    private boolean check() {
        if (mc.player == null || mc.world == null) {
            return true;
        }

        didPlace = false;
        placements = 0;
        obbySlot = InventoryUtil.getBlockInHotbar(Blocks.OBSIDIAN);

        if (retryTimer.passed(retryDelay.getValue())) {
            retries.clear();
            retryTimer.reset();
        }

        if (obbySlot == -1) {
            obbySlot = InventoryUtil.getBlockInHotbar(Blocks.ENDER_CHEST);
            if (obbySlot == -1) {
                this.setEnabled(false);
                return true;
            }
        }

        return !timer.passed(delay.getValue());
    }

    private void placeBlock(BlockPos pos) {
        if (placements < blocksPerTick.getValue()) {
            placeBlock2(pos);
            didPlace = true;
            placements++;
        }
    }
    public static void placeBlock2(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            if (BlockUtil.canBeClicked(neighbor)) {
                Vec3d hitVec = (new Vec3d((Vec3i) neighbor)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(side2.getDirectionVec())).scale(0.5D));
                if (eyesPos.squareDistanceTo(hitVec) <= 18.0625D) {
                    HoleFillUtil.faceVectorPacketInstant(hitVec);
                    mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.rightClickDelayTimer = 4;

                    return;
                }
            }
        }
    }
}