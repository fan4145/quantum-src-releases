package me.derp.quantum.features.modules.render;

import me.derp.quantum.event.events.Render3DEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.modules.client.ClickGui;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.BlockUtil;
import me.derp.quantum.util.ColorUtil;
import me.derp.quantum.util.RenderUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;


public class HoleESP extends Module {
    public Setting<Boolean> renderOwn = register(new Setting<>("RenderOwn", true));
    public Setting<Boolean> fov = register(new Setting<>("InFov", true));
    public Setting<Boolean> rainbow = register(new Setting<>("Rainbow", false));
    private final Setting<Integer> range = register(new Setting<>("RangeX", 0, 0, 10));
    private final Setting<Integer> rangeY = register(new Setting<>("RangeY", 0, 0, 10));
    public Setting<Boolean> box = register(new Setting<>("Box", true));
    public Setting<Boolean> gradientBox = register(new Setting<Object>("Gradient", Boolean.FALSE, v -> box.getValue()));
    public Setting<Boolean> invertGradientBox = register(new Setting<Object>("ReverseGradient", Boolean.FALSE, v -> gradientBox.getValue()));
    public Setting<Boolean> outline = register(new Setting<>("Outline", true));
    public Setting<Boolean> gradientOutline = register(new Setting<Object>("GradientOutline", Boolean.FALSE, v -> outline.getValue()));
    public Setting<Boolean> invertGradientOutline = register(new Setting<Object>("ReverseOutline", Boolean.FALSE, v -> gradientOutline.getValue()));
    public Setting<Double> height = register(new Setting<>("Height", 0.0, -2.0, 2.0));
    private final Setting<Integer> red = register(new Setting<>("Red", 0, 0, 255));
    private final Setting<Integer> green = register(new Setting<>("Green", 255, 0, 255));
    private final Setting<Integer> blue = register(new Setting<>("Blue", 0, 0, 255));
    private final Setting<Integer> alpha = register(new Setting<>("Alpha", 255, 0, 255));
    private final Setting<Integer> boxAlpha = register(new Setting<Object>("BoxAlpha", 125, 0, 255, v -> box.getValue()));
    private final Setting<Float> lineWidth = register(new Setting<Object>("LineWidth", 1.0f, 0.1f, 5.0f, v -> outline.getValue()));
    public Setting<Boolean> safeColor = register(new Setting<>("BedrockColor", false));
    private final Setting<Integer> safeRed = register(new Setting<Object>("BedrockRed", 0, 0, 255, v -> safeColor.getValue()));
    private final Setting<Integer> safeGreen = register(new Setting<Object>("BedrockGreen", 255, 0, 255, v -> safeColor.getValue()));
    private final Setting<Integer> safeBlue = register(new Setting<Object>("BedrockBlue", 0, 0, 255, v -> safeColor.getValue()));
    private final Setting<Integer> safeAlpha = register(new Setting<Object>("BedrockAlpha", 255, 0, 255, v -> safeColor.getValue()));
    public Setting<Boolean> customOutline = register(new Setting<Object>("CustomLine", Boolean.FALSE, v -> outline.getValue()));
    private final Setting<Integer> cRed = register(new Setting<Object>("OL-Red", 0, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cGreen = register(new Setting<Object>("OL-Green", 0, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cBlue = register(new Setting<Object>("OL-Blue", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> cAlpha = register(new Setting<Object>("OL-Alpha", 255, 0, 255, v -> customOutline.getValue() && outline.getValue()));
    private final Setting<Integer> safecRed = register(new Setting<Object>("OL-SafeRed", 0, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));
    private final Setting<Integer> safecGreen = register(new Setting<Object>("OL-SafeGreen", 255, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));
    private final Setting<Integer> safecBlue = register(new Setting<Object>("OL-SafeBlue", 0, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));
    private final Setting<Integer> safecAlpha = register(new Setting<Object>("OL-SafeAlpha", 255, 0, 255, v -> customOutline.getValue() && outline.getValue() && safeColor.getValue()));
    private static HoleESP INSTANCE = new HoleESP();

    public HoleESP() {
        super("HoleESP", "Shows safe spots.", Module.Category.RENDER, false, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static HoleESP getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HoleESP();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        assert (HoleESP.mc.renderViewEntity != null);
        Vec3i playerPos = new Vec3i(HoleESP.mc.renderViewEntity.posX, HoleESP.mc.renderViewEntity.posY, HoleESP.mc.renderViewEntity.posZ);
        for (int x = playerPos.getX() - range.getValue(); x < playerPos.getX() + range.getValue(); ++x) {
            for (int z = playerPos.getZ() - range.getValue(); z < playerPos.getZ() + range.getValue(); ++z) {
                for (int y = playerPos.getY() + rangeY.getValue(); y > playerPos.getY() - rangeY.getValue(); --y) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!HoleESP.mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) || !HoleESP.mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) || !HoleESP.mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) || pos.equals(new BlockPos(HoleESP.mc.player.posX, HoleESP.mc.player.posY, HoleESP.mc.player.posZ)) && !renderOwn.getValue() || !BlockUtil.isPosInFov(pos) && fov.getValue())
                        continue;
                    int currentAlpha = 0;
                    if (HoleESP.mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK && HoleESP.mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK && HoleESP.mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && HoleESP.mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && HoleESP.mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                        RenderUtil.drawBoxESP(pos, rainbow.getValue() ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(safeRed.getValue(), safeGreen.getValue(), safeBlue.getValue(), safeAlpha.getValue()), customOutline.getValue(), new Color(safecRed.getValue(), safecGreen.getValue(), safecBlue.getValue(), safecAlpha.getValue()), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), currentAlpha);
                        continue;
                    }
                    if (BlockUtil.isBlockUnSafe(HoleESP.mc.world.getBlockState(pos.down()).getBlock()) || BlockUtil.isBlockUnSafe(HoleESP.mc.world.getBlockState(pos.east()).getBlock()) || BlockUtil.isBlockUnSafe(HoleESP.mc.world.getBlockState(pos.west()).getBlock()) || BlockUtil.isBlockUnSafe(HoleESP.mc.world.getBlockState(pos.south()).getBlock()) || BlockUtil.isBlockUnSafe(HoleESP.mc.world.getBlockState(pos.north()).getBlock()))
                        continue;
                    RenderUtil.drawBoxESP(pos, rainbow.getValue() ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), customOutline.getValue(), new Color(cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cAlpha.getValue()), lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), currentAlpha);
                }
            }
        }
    }
}