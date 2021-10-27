package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.TotemPopEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class PopChams
        extends Module {
    public static PopChams INSTANCE = new PopChams();
    public Setting<Integer> red = this.register(new Setting<Object>("Red", 0, 0, 255));
    public Setting<Integer> green = this.register(new Setting<Object>("Green", 255, 0, 255));
    public Setting<Integer> blue = this.register(new Setting<Object>("Blue", 0, 0, 255));
    public Setting<Integer> alpha = this.register(new Setting<>("Alpha", 255, 0, 255));

    public PopChams() {
        super("PopChams", "1 origianl module from kambing", Module.Category.RENDER, false, false, false);
    }

    public static PopChams getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new PopChams();
        }
        return INSTANCE;
    }

    public void setInstance() {
        INSTANCE = this;
    }

    public static ConcurrentHashMap<Integer, Integer> pops = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onPopLol(TotemPopEvent event) {
        Color color = EntityUtil.getColor(event.getEntity(), this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue(), false);
        if (event.getEntity() != mc.player) {
            Entity ee = event.getEntity();
            Command.sendMessage("PopEventLol"); // this looks like wurst+ 3
            ArrayList<Integer> idList = new ArrayList<>();
            for (Entity e : mc.world.loadedEntityList) {
                idList.add(e.entityId);
            }
            EntityOtherPlayerMP popCham = new EntityOtherPlayerMP(mc.world, event.getEntity().getGameProfile());
            popCham.copyLocationAndAnglesFrom(ee);
            popCham.rotationYawHead = ee.getRotationYawHead();
            popCham.rotationYaw = ee.rotationYaw;
            popCham.rotationPitch = ee.rotationPitch;
            popCham.setGameType(GameType.CREATIVE);
            popCham.setHealth(20);
            for (int i = 0; i > -10000; i--) {
                if (!idList.contains(i)) {
                    mc.world.addEntityToWorld(i, popCham);
                    pops.put(i, color.getAlpha());
                    break;
                }
            }
        }
    }
}
