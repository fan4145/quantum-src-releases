package me.derp.quantum.features.modules.movement;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.MathUtil;
import me.derp.quantum.util.PlayerUtil;
import me.derp.quantum.util.Timer;
import me.derp.quantum.util.Util;

public
class Speed
        extends Module {
    private final Timer timer = new Timer();
    Setting<Mode> mode = this.register(new Setting<>("Mode", Mode.yPort));
    Setting<Double> yPortSpeed = this.register(new Setting<>("YPort Speed", 0.6, 0.5, 1.5, v -> this.mode.getValue() == Mode.yPort));
    Setting<Boolean> step = this.register(new Setting<>("Step", true, v -> this.mode.getValue() == Mode.yPort));
    Setting<Double> vanillaSpeed = this.register(new Setting<>("Vanilla Speed", 1.0, 1.7, 10.0, v -> this.mode.getValue() == Mode.Vanilla));

    public Speed() {
        super("Speed", "YPort Speed.", Module.Category.MOVEMENT, false, false, false);
    }
    public boolean hop;
    private double prevY;
    public boolean move;

    private void jump() {
        this.hop = true;
        this.prevY = Util.mc.player.posY;
        Util.mc.player.jump();
    }
    
    @Override
    public void onEnable() {
        PlayerUtil.getBaseMoveSpeed();
        if (this.step.getValue()) {
            if (Speed.fullNullCheck()) {
                return;
            }
            Speed.mc.player.stepHeight = 2.0f;
        }
    }

    @Override
    public void onDisable() {
        Quantum.timerManager.reset();
        this.timer.reset();
        if (this.step.getValue()) {
            Speed.mc.player.stepHeight = 0.6f;
        }
    }

    @Override
    public void onUpdate() {
        if (Speed.nullCheck()) {
            this.disable();
            return;
        }
        if (this.mode.getValue() == Mode.Vanilla) {
            if (Speed.mc.player == null || Speed.mc.world == null) {
                return;
            }
            double[] calc = MathUtil.directionSpeed(this.vanillaSpeed.getValue() / 10.0);
            Speed.mc.player.motionX = calc[0];
            Speed.mc.player.motionZ = calc[1];
        }
        if (this.mode.getValue() == Mode.yPort) {
            if (!PlayerUtil.isMoving(Speed.mc.player) || Speed.mc.player.isInWater() && Speed.mc.player.isInLava() || Speed.mc.player.collidedHorizontally) {
                return;
            }
            if (Speed.mc.player.onGround) {
                Quantum.timerManager.setTimer(1.15f);
                Speed.mc.player.jump();
                PlayerUtil.setSpeed(Speed.mc.player, PlayerUtil.getBaseMoveSpeed() + this.yPortSpeed.getValue() / 10.0);
            } else {
                Speed.mc.player.motionY = -1.0;
                Quantum.timerManager.reset();
            }
        }
        if (this.mode.getValue() == Mode.onGround) {
            if ((this.hop) & (Util.mc.player.posY >= this.prevY + 0.399994D)) {
                Util.mc.player.motionY = -0.9D;
                Util.mc.player.posY = this.prevY;
                this.hop = false;
            }
            if ((Util.mc.player.moveForward != 0.0F) & (!Util.mc.player.collidedHorizontally)) {
                if ((Util.mc.player.moveForward == 0.0F) & (Util.mc.player.moveStrafing == 0.0F)) {
                    Util.mc.player.motionX = 0.0D;
                    Util.mc.player.motionZ = 0.0D;
                    if (Util.mc.player.collidedVertically) {
                        Util.mc.player.jump();
                        this.move = true;
                    }
                    if ((this.move) & (Util.mc.player.collidedVertically))
                        this.move = false;
                }
                if (Util.mc.player.collidedVertically) {
                    Util.mc.player.motionX *= 1.0379D;
                    Util.mc.player.motionZ *= 1.0379D;
                    jump();
                }
                if ((this.hop) & (!this.move) & (Util.mc.player.posY >= this.prevY + 0.399994D)) {
                    Util.mc.player.motionY = -100.0D;
                    Util.mc.player.posY = this.prevY;
                    this.hop = false;
                }

            }

        }
    }

    public
    enum Mode {
        yPort,
        Vanilla,
        onGround
    }
}