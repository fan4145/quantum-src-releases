package me.derp.quantum.features.modules.render;

import me.derp.quantum.event.events.Render3DEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.EntityUtil;
import me.derp.quantum.util.RenderUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CityEsp extends Module {

    public Setting<Boolean> end_crystal = this.register(new Setting<>("EndCrystal", true));
    public Setting<Integer> range = this.register(new Setting<>("Range", 6, 0, 12));
    public Setting<Boolean> render = this.register(new Setting<>("Render", true));
    public Setting<Boolean> colorSync = this.register(new Setting<>("ColorSync", true, v -> this.render.getValue()));
    public Setting<Integer> red = this.register(new Setting<>("Red", 255, 1, 255, v -> this.render.getValue()));
    public Setting<Integer> green = this.register(new Setting<>("Green", 255, 1, 255, v -> this.render.getValue()));
    public Setting<Integer> blue = this.register(new Setting<>("Blue", 255, 1, 255, v -> this.render.getValue()));
    public Setting<Integer> alpha = this.register(new Setting<>("Alpha", 125, 1, 255, v -> this.render.getValue()));

    List<BlockPos> blocks = new ArrayList<>();

    public CityEsp() {
        super("CityEsp", "citi e es pee", Category.RENDER, true, false, false);
    }

    @Override
    public void onEnable() {
        blocks.clear();
    }

    @Override
    public void onUpdate() {
        blocks.clear();
        for (EntityPlayer player : mc.world.playerEntities) {
            if (mc.player.getDistance(player) > range.getValue() || mc.player == player) continue;

            BlockPos p = EntityUtil.is_cityable(player, end_crystal.getValue());

            if (p != null) {
                blocks.add(p);
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (BlockPos pos : blocks) {
            RenderUtil.drawBox(pos, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()));
        }
    }

    @Override
    public void onDisable() {
        blocks.clear();
    }
}
