package me.derp.quantum.features.modules.troll;

import me.derp.quantum.Quantum;
import me.derp.quantum.event.events.UpdateWalkingPlayerEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.BlockUtil;
import me.derp.quantum.util.MathUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public
class Lawnmower extends Module {
    public Setting <Integer> playerRange = new Setting <>("Range", 2, 0, 6);
    public Setting <Integer> playerHeight = new Setting <>("Height", 2, 0, 6);
    public Setting <Boolean> rotate = new Setting<>("Rotate", true);
    public Lawnmower() {
        super("LawnMower", "haha funny meme", Category.TROLL, true, false, false);
        register(playerRange);
        register(playerHeight);
        register(rotate);
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer ( UpdateWalkingPlayerEvent event ) {
        for (BlockPos pos : BlockUtil.getSphere(mc.player.getPosition(), playerRange.getValue(), playerHeight.getValue(), false, true, 0)) {
            if (!check(pos)) continue;
            if (pos != null) {
                if (rotate.getValue()) {
                    float[] angle = MathUtil.calcAngle ( mc.player.getPositionEyes ( mc.getRenderPartialTicks ( ) ) , new Vec3d( (float) pos.getX ( )  , (float) pos.getY ( ) , (float) pos.getZ ( )  ) );
                    Quantum.rotationManager.setPlayerRotations ( angle[0] , angle[1] );
                }
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
            }
        }
    }

    boolean check(final BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.TALLGRASS || mc.world.getBlockState(pos).getBlock() == Blocks.DOUBLE_PLANT;
    }
}