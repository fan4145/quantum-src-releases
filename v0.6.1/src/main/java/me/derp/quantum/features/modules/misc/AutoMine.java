package me.derp.quantum.features.modules.misc;

import me.derp.quantum.features.modules.Module;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;


public class AutoMine extends Module {

    public AutoMine() {
        super("AutoMine", "for lazy ppl who want to mine", Category.MISC, true, false, false);
    }

    public void onUpdate() {
        if (fullNullCheck()) return;
      if(mc.objectMouseOver!= null)mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,mc.objectMouseOver.getBlockPos(), EnumFacing.UP));
      mc.player.swingArm(EnumHand.MAIN_HAND);







    }





}
