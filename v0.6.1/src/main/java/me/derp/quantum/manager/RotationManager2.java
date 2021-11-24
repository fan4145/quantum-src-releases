package me.derp.quantum.manager;

import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.util.Util;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/*
* this supposed to be new rotaiton manager :)
 */
public class RotationManager2 implements Util {
    boolean rotated = false;
    float yaw = 0,pitch = 0;
    public RotationManager2() {
        MinecraftForge.EVENT_BUS.register(this);
    }
/*
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && yaw != 0 && pitch != 0) {
            ((CPacketPlayer) event.getPacket()).yaw = this.yaw;
            ((CPacketPlayer) event.getPacket()).pitch = this.pitch;
        }
    }
*/
    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float balls) {
        this.pitch = balls;
    }

    public void setYaw(float sex) {
        this.yaw = sex;
    }

    /*
    public boolean getRotated() {
        return this.rotated;
    }

    public void setRotated(boolean gay) {
        this.rotated = gay;
    }
     */
}
