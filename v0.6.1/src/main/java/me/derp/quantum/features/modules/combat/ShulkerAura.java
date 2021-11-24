package me.derp.quantum.features.modules.combat;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.InventoryUtil;
import me.derp.quantum.util.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ShulkerAura extends Module {

    public ShulkerAura() {
        super("ShulkerAura", "Uses shulkers to push crystals into enemies", Category.COMBAT, true, false, false);
    }

    public final List<Block> blackList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.TRAPDOOR, Blocks.ENCHANTING_TABLE);
    public final List<Block> shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);

    private int oldSlot, shulkerSlot, crystalSlot, waitTicks;

    private boolean doShulker, doCrystal, openShulker, detonate, finishedDetonate;

    private BlockPos shulkerSpot, crystalSpot;

    private enum direction {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    private direction spoofDirection;
    private EntityPlayer target;

    private final Setting<Boolean> debug = this.register(new Setting<>("Debug", false));
    private final Setting<Boolean> rotate = this.register(new Setting<>("Rotation Lock", false));
    private final Setting<Boolean> antiWeakness = this.register(new Setting<>("Anti Weakness", false));
    private final Setting<Integer> detonateDelay = this.register(new Setting<>("Detonate Delay", 4, 1, 10));
    private final Setting<Integer> endDelay = this.register(new Setting<>("Await Delay", 4, 1, 10));
    private final Setting<Integer> restartDelay = this.register(new Setting<>("Attempt Delay", 4, 1, 10));

    @Override
    public void onEnable() {
        spoofDirection = direction.NORTH;
        target = null;
        oldSlot = -1;
        doShulker = false;
        doCrystal = false;
        openShulker = false;
        detonate = false;
        finishedDetonate = false;
        crystalSlot = -1;
        shulkerSlot = -1;
        waitTicks = 0;
    }

    private EntityPlayer getTarget() {
        EntityPlayer temp = null;

        for (EntityPlayer player : mc.world.playerEntities) {
            if (player != null && player != mc.player && player.getHealth() > 0 && mc.player.getDistance(player) < 5) {
                if (!Quantum.friendManager.isFriend(player.getName())) {
                    temp = player;
                }
            }
        }

        if (temp != null)
            if (debug.getValue()) Command.sendMessage("Target Set: " + temp.getName());

        return temp;
    }

    @Override
    public void onUpdate() {

        if (doShulker) {
            if (mc.player.inventory.currentItem != shulkerSlot) {
                if (debug.getValue())
                    Command.sendMessage("Swapping to slot " + shulkerSlot);
                mc.player.inventory.currentItem = shulkerSlot;
                return;
            }

            placeBlock(shulkerSpot);

            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            doShulker = false;
            return;
        }

        if (doCrystal) {
            if (mc.player.inventory.currentItem != crystalSlot) {
                if (debug.getValue())
                    Command.sendMessage("Swapping to slot " + crystalSlot);
                mc.player.inventory.currentItem = crystalSlot;
                return;
            }

            placeCrystal(crystalSpot);

            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            doCrystal = false;
            openShulker = true;
            return;
        }

        if (openShulker) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(shulkerSpot, EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
            if (mc.currentScreen instanceof GuiShulkerBox) {
                if (debug.getValue()) Command.sendMessage("Closing Shulker");
                mc.player.closeScreenAndDropStack();
                openShulker = false;
                detonate = true;
                waitTicks = 0;
            }
            return;
        }

        if (detonate) {

            if (waitTicks++ > detonateDelay.getValue()) {

                if (waitTicks - detonateDelay.getValue() > restartDelay.getValue()) {
                    if (debug.getValue()) Command.sendMessage("Re-Attempting");
                    detonate = false;
                    doCrystal = true;
                }

                for (Entity e : mc.world.loadedEntityList) {
                    if (e instanceof EntityEnderCrystal) {
                        if (!e.isDead) {
                            if (mc.player.getDistance(e) < 5) {
                                mc.player.connection.sendPacket(new CPacketUseEntity(e));
                                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                                detonate = false;
                                finishedDetonate = true;
                                waitTicks = 0;
                            }
                        }
                    }
                }
            }

            return;
        }

        if (finishedDetonate) {
            if (waitTicks++ > endDelay.getValue()) {
                finishedDetonate = false;
            }

            return;
        }

        this.target = getTarget();

        oldSlot = mc.player.inventory.currentItem;

        if (target != null) {

            /*
            h = head / upper body
            b = body / legs
            c = crystal
            s = shulker
            + = full block

            [h][c][s][+]
            [b][+][+][+]
             */

            Vec3d offset1 = target.getPositionVector().add(1, 0, 0); //Full
            Vec3d offset2 = target.getPositionVector().add(2, 0, 0); //Full
            Vec3d offset3 = target.getPositionVector().add(3, 0, 0); //Full
            Vec3d offset4 = target.getPositionVector().add(1, 1, 0); //Air
            Vec3d offset5 = target.getPositionVector().add(2, 1, 0); //Air
            Vec3d offset6 = target.getPositionVector().add(3, 1, 0); //Full

            if (mc.world.getBlockState(new BlockPos(offset1)).getBlock() != Blocks.AIR) {
                if (mc.world.getBlockState(new BlockPos(offset2)).getBlock() != Blocks.AIR) {
                    if (mc.world.getBlockState(new BlockPos(offset3)).getBlock() != Blocks.AIR) {
                        if (mc.world.getBlockState(new BlockPos(offset4)).getBlock() == Blocks.AIR) {
                            if (mc.world.getBlockState(new BlockPos(offset5)).getBlock() == Blocks.AIR || shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                if (mc.world.getBlockState(new BlockPos(offset6)).getBlock() != Blocks.AIR) {
                                    if (debug.getValue())
                                        Command.sendMessage("Target is vulnerable!");

                                    if (debug.getValue())
                                        Command.sendMessage("Method 1");

                                    spoofDirection = direction.EAST;

                                    shulkerSlot = -1;
                                    crystalSlot = -1;

                                    if (shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                        if (debug.getValue())
                                            Command.sendMessage("Shulker already in place.");
                                        shulkerSlot = 1337;
                                    } else {
                                        for (Block b : shulkerList) {
                                            if (findHotbarBlock(b) != -1) {
                                                shulkerSlot = findHotbarBlock(b);
                                                break; //End the loop
                                            }
                                        }
                                    }

                                    crystalSlot = findHotbarItem(Items.END_CRYSTAL);

                                    if (shulkerSlot != -1) {
                                        if (crystalSlot != -1) {
                                            shulkerSpot = new BlockPos(offset5);
                                            crystalSpot = new BlockPos(offset1);
                                            if (!shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock()))
                                                doShulker = true;
                                            doCrystal = true;
                                            return; //Prevent the next checks being executed, they are un-needed
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

                        /*
            h = head / upper body
            b = body / legs
            c = crystal
            s = shulker
            + = full block

            [+][s][c][h]
            [+][+][+][b]
             */

            offset1 = target.getPositionVector().add(-1, 0, 0); //Full
            offset2 = target.getPositionVector().add(-2, 0, 0); //Full
            offset3 = target.getPositionVector().add(-3, 0, 0); //Full
            offset4 = target.getPositionVector().add(-1, 1, 0); //Air
            offset5 = target.getPositionVector().add(-2, 1, 0); //Air
            offset6 = target.getPositionVector().add(-3, 1, 0); //Full

            if (mc.world.getBlockState(new BlockPos(offset1)).getBlock() != Blocks.AIR) {
                if (mc.world.getBlockState(new BlockPos(offset2)).getBlock() != Blocks.AIR) {
                    if (mc.world.getBlockState(new BlockPos(offset3)).getBlock() != Blocks.AIR) {
                        if (mc.world.getBlockState(new BlockPos(offset4)).getBlock() == Blocks.AIR) {
                            if (mc.world.getBlockState(new BlockPos(offset5)).getBlock() == Blocks.AIR || shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                if (mc.world.getBlockState(new BlockPos(offset6)).getBlock() != Blocks.AIR) {
                                    if (debug.getValue())
                                        Command.sendMessage("Target is vulnerable!");

                                    if (debug.getValue())
                                        Command.sendMessage("Method 2");

                                    spoofDirection = direction.WEST;

                                    shulkerSlot = -1;
                                    crystalSlot = -1;

                                    if (shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                        if (debug.getValue())
                                            Command.sendMessage("Shulker already in place.");
                                        shulkerSlot = 1337;
                                    } else {
                                        for (Block b : shulkerList) {
                                            if (findHotbarBlock(b) != -1) {
                                                shulkerSlot = findHotbarBlock(b);
                                                break; //End the loop
                                            }
                                        }
                                    }

                                    crystalSlot = findHotbarItem(Items.END_CRYSTAL);

                                    if (shulkerSlot != -1) {
                                        if (crystalSlot != -1) {
                                            shulkerSpot = new BlockPos(offset5);
                                            crystalSpot = new BlockPos(offset1);
                                            if (!shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock()))
                                                doShulker = true;
                                            doCrystal = true;
                                            return; //Prevent the next checks being executed, they are un-needed
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            offset1 = target.getPositionVector().add(0, 0, 1); //Full
            offset2 = target.getPositionVector().add(0, 0, 2); //Full
            offset3 = target.getPositionVector().add(0, 0, 3); //Full
            offset4 = target.getPositionVector().add(0, 1, 1); //Air
            offset5 = target.getPositionVector().add(0, 1, 2); //Air
            offset6 = target.getPositionVector().add(0, 1, 3); //Full

            if (mc.world.getBlockState(new BlockPos(offset1)).getBlock() != Blocks.AIR) {
                if (mc.world.getBlockState(new BlockPos(offset2)).getBlock() != Blocks.AIR) {
                    if (mc.world.getBlockState(new BlockPos(offset3)).getBlock() != Blocks.AIR) {
                        if (mc.world.getBlockState(new BlockPos(offset4)).getBlock() == Blocks.AIR) {
                            if (mc.world.getBlockState(new BlockPos(offset5)).getBlock() == Blocks.AIR || shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                if (mc.world.getBlockState(new BlockPos(offset6)).getBlock() != Blocks.AIR) {
                                    if (debug.getValue())
                                        Command.sendMessage("Target is vulnerable!");

                                    if (debug.getValue())
                                        Command.sendMessage("Method 3");

                                    spoofDirection = direction.SOUTH;

                                    shulkerSlot = -1;
                                    crystalSlot = -1;

                                    if (shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                        if (debug.getValue())
                                            Command.sendMessage("Shulker already in place.");
                                        shulkerSlot = 1337;
                                    } else {
                                        for (Block b : shulkerList) {
                                            if (findHotbarBlock(b) != -1) {
                                                shulkerSlot = findHotbarBlock(b);
                                                break; //End the loop
                                            }
                                        }
                                    }

                                    crystalSlot = findHotbarItem(Items.END_CRYSTAL);

                                    if (shulkerSlot != -1) {
                                        if (crystalSlot != -1) {
                                            shulkerSpot = new BlockPos(offset5);
                                            crystalSpot = new BlockPos(offset1);
                                            if (!shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock()))
                                                doShulker = true;
                                            doCrystal = true;
                                            return; //Prevent the next checks being executed, they are un-needed
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            offset1 = target.getPositionVector().add(0, 0, -1); //Full
            offset2 = target.getPositionVector().add(0, 0, -2); //Full
            offset3 = target.getPositionVector().add(0, 0, -3); //Full
            offset4 = target.getPositionVector().add(0, 1, -1); //Air
            offset5 = target.getPositionVector().add(0, 1, -2); //Air
            offset6 = target.getPositionVector().add(0, 1, -3); //Full

            if (mc.world.getBlockState(new BlockPos(offset1)).getBlock() != Blocks.AIR) {
                if (mc.world.getBlockState(new BlockPos(offset2)).getBlock() != Blocks.AIR) {
                    if (mc.world.getBlockState(new BlockPos(offset3)).getBlock() != Blocks.AIR) {
                        if (mc.world.getBlockState(new BlockPos(offset4)).getBlock() == Blocks.AIR) {
                            if (mc.world.getBlockState(new BlockPos(offset5)).getBlock() == Blocks.AIR || shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                if (mc.world.getBlockState(new BlockPos(offset6)).getBlock() != Blocks.AIR) {
                                    if (debug.getValue())
                                        Command.sendMessage("Target is vulnerable!");

                                    if (debug.getValue())
                                        Command.sendMessage("Method 4");

                                    spoofDirection = direction.NORTH;

                                    shulkerSlot = -1;
                                    crystalSlot = -1;

                                    if (shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock())) {
                                        if (debug.getValue())
                                            Command.sendMessage("Shulker already in place.");
                                        shulkerSlot = 1337;
                                    } else {
                                        for (Block b : shulkerList) {
                                            if (findHotbarBlock(b) != -1) {
                                                shulkerSlot = findHotbarBlock(b);
                                                break; //End the loop
                                            }
                                        }
                                    }

                                    crystalSlot = findHotbarItem(Items.END_CRYSTAL);

                                    if (shulkerSlot != -1) {
                                        if (crystalSlot != -1) {
                                            shulkerSpot = new BlockPos(offset5);
                                            crystalSpot = new BlockPos(offset1);
                                            if (!shulkerList.contains(mc.world.getBlockState(new BlockPos(offset5)).getBlock()))
                                                doShulker = true;
                                            doCrystal = true;
                                            return; //Prevent the next checks being executed, they are un-needed
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //end
        }
    }

    public int findHotbarItem(Item itemIn) {
        for (int i = 0; i < 9; ++i) {
            Item item;
            ItemStack stack = InventoryUtil.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || (item = stack.getItem()) != itemIn)
                continue;
            return i;
        }
        return -1;
    }

    public int findHotbarBlock(Block blockIn) {
        for (int i = 0; i < 9; ++i) {
            Block block;
            ItemStack stack = InventoryUtil.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock) || (block = ((ItemBlock) stack.getItem()).getBlock()) != blockIn)
                continue;
            return i;
        }
        return -1;
    }

    private void placeCrystal(BlockPos pos) {
        if (debug.getValue())
            Command.sendMessage("Debug " + pos); //yes
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
    }

    private void placeBlock(BlockPos pos) {
        if (spoofDirection == direction.NORTH) {
            if (rotate.getValue()) {
                mc.player.rotationYaw = 180;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(180, 0, mc.player.onGround));
        } else if (spoofDirection == direction.SOUTH) {
            if (rotate.getValue()) {
                mc.player.rotationYaw = 0;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, 0, mc.player.onGround));
        } else if (spoofDirection == direction.WEST) {
            if (rotate.getValue()) {
                mc.player.rotationYaw = 90;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(90, 0, mc.player.onGround));
        } else if (spoofDirection == direction.EAST) {
            if (rotate.getValue()) {
                mc.player.rotationYaw = -90;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(-90, 0, mc.player.onGround));
        }

        boolean isSneaking = placeBlock(pos, EnumHand.MAIN_HAND, false, true, mc.player.isSneaking());

        if (isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

    }

    public boolean placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking) {
        boolean sneaking = false;
        EnumFacing side = getFirstFacing(pos);
        if (side == null) {
            return isSneaking;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if (!mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
            sneaking = true;
        }
        if (rotate) {
            RotationUtil.faceVector(hitVec, true);
        }

        rightClickBlock(neighbour, hitVec, hand, opposite, packet);

        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        mc.rightClickDelayTimer = 4;
        return sneaking || isSneaking;
    }

    public EnumFacing getFirstFacing(BlockPos pos) {
        Iterator<EnumFacing> iterator = getPossibleSides(pos).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public List<EnumFacing> getPossibleSides(BlockPos pos) {
        ArrayList<EnumFacing> facings = new ArrayList<>();

        List<EnumFacing> directions = new ArrayList<>();
        directions.add(EnumFacing.NORTH);
        directions.add(EnumFacing.SOUTH);
        directions.add(EnumFacing.EAST);
        directions.add(EnumFacing.WEST);

        for (EnumFacing side : directions) {
            IBlockState blockState;
            BlockPos neighbour = pos.offset(side);
            if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false) || (blockState = mc.world.getBlockState(neighbour)).getMaterial().isReplaceable())
                continue;
            facings.add(side);
        }
        return facings;
    }

    public void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            float f = (float) (vec.x - (double) pos.getX());
            float f1 = (float) (vec.y - (double) pos.getY());
            float f2 = (float) (vec.z - (double) pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
        }
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4;
    }

}
