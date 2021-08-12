package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.MathUtil;
import me.alpha432.oyvey.util.PlayerUtil;
import me.alpha432.oyvey.util.Timer;

public
class Speed
        extends Module {
    private final Timer timer = new Timer ( );
    Setting < Mode > mode = this.register ( new Setting <> ( "Mode" , Mode.yPort ) );
    Setting < Double > yPortSpeed = this.register ( new Setting <> ( "YPort Speed" , 0.6 , 0.5 , 1.5 , v -> this.mode.getValue ( ) == Mode.yPort ) );
    Setting < Boolean > step = this.register ( new Setting <> ( "Step" , true , v -> this.mode.getValue ( ) == Mode.yPort ) );
    Setting < Double > vanillaSpeed = this.register ( new Setting <> ( "Vanilla Speed" , 1.0 , 1.7 , 10.0 , v -> this.mode.getValue ( ) == Mode.Vanilla ) );

    public
    Speed ( ) {
        super ( "Speed" , "YPort Speed." , Module.Category.MOVEMENT , false , false , false );
    }

    @Override
    public
    void onEnable ( ) {
        PlayerUtil.getBaseMoveSpeed ( );
        if ( this.step.getValue ( ) ) {
            if ( Speed.fullNullCheck ( ) ) {
                return;
            }
            Speed.mc.player.stepHeight = 2.0f;
        }
    }

    @Override
    public
    void onDisable ( ) {
        OyVey.timerManager.reset ( );
        this.timer.reset ( );
        if ( this.step.getValue ( ) ) {
            Speed.mc.player.stepHeight = 0.6f;
        }
    }

    @Override
    public
    void onUpdate ( ) {
        if ( Speed.nullCheck ( ) ) {
            this.disable ( );
            return;
        }
        if ( this.mode.getValue ( ) == Mode.Vanilla ) {
            if ( Speed.mc.player == null || Speed.mc.world == null ) {
                return;
            }
            double[] calc = MathUtil.directionSpeed ( this.vanillaSpeed.getValue ( ) / 10.0 );
            Speed.mc.player.motionX = calc[0];
            Speed.mc.player.motionZ = calc[1];
        }
        if ( this.mode.getValue ( ) == Mode.yPort ) {
            if ( ! PlayerUtil.isMoving ( Speed.mc.player ) || Speed.mc.player.isInWater ( ) && Speed.mc.player.isInLava ( ) || Speed.mc.player.collidedHorizontally ) {
                return;
            }
            if ( Speed.mc.player.onGround ) {
                OyVey.timerManager.setTimer ( 1.15f );
                Speed.mc.player.jump ( );
                PlayerUtil.setSpeed ( Speed.mc.player , PlayerUtil.getBaseMoveSpeed ( ) + this.yPortSpeed.getValue ( ) / 10.0 );
            } else {
                Speed.mc.player.motionY = - 1.0;
                OyVey.timerManager.reset ( );
            }
        }
    }

    public
    enum Mode {
        yPort,
        Vanilla

    }
}