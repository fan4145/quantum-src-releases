package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public
class Lawnmower extends Module {
    public Setting <Integer> playerRange = new Setting <>("Range", 6, 0, 6);
    public Setting <Integer> playerHeight = new Setting <>("Height", 6, 0, 6);

    public
    Lawnmower() {
        super("LawnMower", "haha funny meme", Category.MISC, true, false, false);
    }

    @Override
    public
    void onUpdate() {
        for (BlockPos pos : BlockUtil.getSphere(mc.player.getPosition(), playerRange.getValue(), playerHeight.getValue(), false, true, 0)) {
            if (!check(pos)) continue;
            if (pos != null) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
            }
        }
    }

    boolean check(final BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.TALLGRASS || mc.world.getBlockState(pos).getBlock() == Blocks.DOUBLE_PLANT;
    }
}