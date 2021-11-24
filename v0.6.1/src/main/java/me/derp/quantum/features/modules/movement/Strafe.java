package me.derp.quantum.features.modules.movement;

import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.EntityUtil;
import me.derp.quantum.util.PlayerUtil;
import me.derp.quantum.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class Strafe extends Module {

    Setting<Double> jumpHeight = this.register(new Setting<>("Jump Height", 0.41, 0.0, 1.0));
    Setting<Double> timerVal = this.register(new Setting<>("Timer Speed", 1.15, 1.0, 1.5));

    private double playerSpeed;
    private final Timer timer = new Timer();

    public Strafe() {
        super("Strafe", "lightspeed", Category.MOVEMENT, true, false, false);
    }
    @Override
    public void onEnable() {
        playerSpeed = EntityUtil.getBaseMoveSpeed();
    }

    @Override
    public void onDisable() {
        resetTimer();
        this.timer.reset();
    }

    public static void resetTimer() {
        mc.timer.tickLength = 50;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) {
            this.disable();
            return;
        }

        if (mc.player.isInLava() || mc.player.isInWater() || mc.player.isOnLadder() || mc.player.isInWeb) {
            return;
        }
        double speedY = jumpHeight.getValue();

        if (mc.player.onGround && PlayerUtil.isMoving(mc.player) && timer.passedMs(300)) {
            setTimer(timerVal.getValue().floatValue());
            if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                speedY += (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1) * 0.1f;
            }
            mc.player.motionY = speedY;
            playerSpeed = PlayerUtil.getBaseMoveSpeed() * (isColliding(0, -0.5, 0) instanceof BlockLiquid && !EntityUtil.isInLiquid() ? 0.9 : 1.901);
            boolean slowdown = true;
            timer.reset();
        }
        playerSpeed = Math.max(playerSpeed, PlayerUtil.getBaseMoveSpeed());
        double[] dir = PlayerUtil.forward(playerSpeed);
        mc.player.motionX = dir[0];
        mc.player.motionZ = dir[1];

    }

    public static void setTimer(float speed) {
        mc.timer.tickLength = 50.0f / speed;
    }

    public static Block isColliding(double posX, double posY, double posZ) {
        Block block = null;
        if (mc.player != null) {
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(posX, posY, posZ) : mc.player.getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(posX, posY, posZ);
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
                    block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                }
            }
        }
        return block;
    }
}