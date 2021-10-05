package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.*;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSign;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AutoSignKick extends Module {
    // author: noatmc and derpyjibs :D (finished at 14:51 GMT+ 7 16/09/2021)
    Setting<Integer> switchDelay = this.register(new Setting<>("SwitchDelay", 100, 0, 5000));
    Setting<Integer>  placeDelay = this.register(new Setting<>("PlaceDelay", 100, 0, 5000));
    Setting<Integer>  mineDelay = this.register(new Setting<>("MineDelay", 100, 0, 5000));
    Setting<Integer> range = this.register(new Setting<>("Range", 2, 1, 20));
    Timer placeTimer = new Timer();
    Timer switchTimer = new Timer();
    Timer mineTimer = new Timer();
    public AutoSignKick() {
        super("AutoSignKick", "stop being lazy lmao", Category.MISC, true, false, false);
    }

    private boolean hadBreak;

    @Override
    public void onEnable() {
        hadBreak = false;
    }

    @Override
    public void onDisable() {
        hadBreak = false;
    }


    public void onUpdate() {
        for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
            if (!(tileEntity instanceof TileEntitySign)) continue;
            if (!(mc.player.getDistanceSq(tileEntity.getPos()) <= MathUtill.square(range.getValue()) )) continue;
            Command.sendMessage("Sign located at X: " + tileEntity.getPos().getX() + ", Y: " + tileEntity.getPos().getY() + ", Z: " + tileEntity.getPos().getZ());
            BlockPos posTile = tileEntity.getPos();
            if (!hadBreak) {
                axeSwitch();
                mineBlock(posTile);
                InventoryUtil.switchToHotbarSlot(ItemSign.class, false);
                Command.sendMessage("Changed to sign hotbar.");
                switchTimer.reset();
                place(posTile);
                Command.sendMessage("Done!");
                this.disable();
                hadBreak = true;
            }
        }
    }

    private void axeSwitch() {
        if (switchTimer.passedMs(switchDelay.getValue().longValue() * 3)) {
            InventoryUtil.switchToHotbarSlot(ItemAxe.class, false);
            Command.sendMessage("Switched to Axe");
            switchTimer.reset();
        }
    }

    private void mineBlock(BlockPos pos) {
        if (mineTimer.passedMs(mineDelay.getValue().longValue() * 3)) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
            Command.sendMessage("Mined");
            mineTimer.reset();
        }
    }

    private void place(BlockPos pos) {
        if (placeTimer.passedMs(placeDelay.getValue().longValue() * 3)) {
            BlockUtil.placeBlockSmartRotate(pos, EnumHand.MAIN_HAND, true, true, false);
            Command.sendMessage("Placed sign!");
            placeTimer.reset();
        }
    }
}
