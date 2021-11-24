package me.derp.quantum.features.modules.misc;

import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class AutoFish extends Module {

    public boolean cast;
    public boolean nospam = false;

    public Setting<Boolean> swing = this.register(new Setting <>("Swing", true));

    public AutoFish() {
        super("AutoFish", "this is how u can get good enchant books on oldfag.org", Category.MISC, true, false, false);

    }
    @Override
    public void onEnable() {
        cast = true;
        nospam = true;
    }
//cast part
    public void onTick() {
        if (cast && nospam) {
            mc.rightClickMouse();
            if (swing.getValue()) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
            nospam = false;
        }
    }

//reel part
    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {

            SPacketSoundEffect packet = event.getPacket();
            if (packet.getSound() == SoundEvents.ENTITY_BOBBER_SPLASH) {
                mc.rightClickMouse();
                if (swing.getValue()) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                }
                cast = true;
                nospam = true;

            }

            }

        }

        }



























































//oldfag.org gamers understand y this is a module