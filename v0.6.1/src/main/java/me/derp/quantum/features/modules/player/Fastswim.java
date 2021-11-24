package me.derp.quantum.features.modules.player;

import me.derp.quantum.event.events.MoveEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Fastswim extends Module {
    public Setting<Double> waterHorizontal = this.register(new Setting<Double>("WaterHorizontal", 3.0, 1.0, 20.0));
    public Setting<Double> waterVertical = this.register(new Setting<Double>("WaterVertical", 3.0, 1.0, 20.0));
    public Setting<Double> lavaHorizontal = this.register(new Setting<Double>("LavaHorizontal", 4.0, 1.0, 20.0));
    public Setting<Double> lavaVertical = this.register(new Setting<Double>("LavaVertical", 4.0, 1.0, 20.0));

    public Fastswim() {
        super("FastSwim", "Swim fast", Module.Category.MOVEMENT, true, false, false);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (Fastswim.mc.player.isInLava() && !Fastswim.mc.player.onGround) {
            event.setX(event.getX() * this.lavaHorizontal.getValue());
            event.setZ(event.getZ() * this.lavaHorizontal.getValue());
            event.setY(event.getY() * this.lavaVertical.getValue());
        } else if (Fastswim.mc.player.isInWater() && !Fastswim.mc.player.onGround) {
            event.setX(event.getX() * this.waterHorizontal.getValue());
            event.setZ(event.getZ() * this.waterHorizontal.getValue());
            event.setY(event.getY() * this.waterVertical.getValue());
        }
    }
}

