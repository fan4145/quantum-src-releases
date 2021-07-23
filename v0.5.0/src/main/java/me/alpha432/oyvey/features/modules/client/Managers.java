package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.ClientEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.TextUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public
class Managers extends Module {

    private static Managers INSTANCE = new Managers ( );
    public Setting< Boolean > betterFrames = register ( new Setting ( "BetterMaxFPS" , false ) );
    public Setting < Integer > betterFPS = register ( new Setting ( "MaxFPS" , 300 , 30 , 1000 , v -> betterFrames.getValue ( ) ) );
    public Setting < Boolean > potions = register ( new Setting ( "Potions" , true ) );
    public Setting < Integer > textRadarUpdates = register ( new Setting ( "TRUpdates" , 500 , 0 , 1000 ) );
    public Setting < Integer > respondTime = register ( new Setting ( "SeverTime" , 500 , 0 , 1000 ) );
    public Setting < Float > holeRange = register ( new Setting ( "HoleRange" , 6.0f , 1.0f , 32.0f ) );
    public Setting < Boolean > speed = register ( new Setting ( "Speed" , true ) );
    public Setting < Boolean > tRadarInv = register ( new Setting ( "TRadarInv" , true ) );
    public Setting < Boolean > unfocusedCpu = register ( new Setting ( "UnfocusedCPU" , false ) );
    public Setting < Integer > cpuFPS = register ( new Setting ( "UnfocusedFPS" , 60 , 1 , 60 , v -> unfocusedCpu.getValue ( ) ) );
    public Setting < Boolean > safety = this.register ( new Setting < Boolean > ( "SafetyPlayer" , false ) );
    public Setting < Integer > safetyCheck = this.register ( new Setting < Integer > ( "SafetyCheck" , 50 , 1 , 150 ) );
    public Setting < Integer > safetySync = this.register ( new Setting < Integer > ( "SafetySync" , 250 , 1 , 10000 ) );
    public Setting < Boolean > oneDot15 = this.register ( new Setting < Boolean > ( "1.15" , false ) );
    public Setting < Integer > holeUpdates = this.register ( new Setting < Integer > ( "HoleUpdates" , 100 , 0 , 1000 ) );
    public Setting < Integer > holeSync = this.register ( new Setting < Integer > ( "HoleSync" , 10000 , 1 , 10000 ) );
    public Setting < ThreadMode > holeThread = this.register ( new Setting < ThreadMode > ( "HoleThread" , ThreadMode.WHILE ) );

    public TextUtil.Color bracketColor = TextUtil.Color.WHITE;
    public TextUtil.Color commandColor = TextUtil.Color.DARK_PURPLE;
    public String commandBracket = "[";
    public String commandBracket2 = "]";
    public String command = "Quantum";
    public int moduleListUpdates = 0;
    public boolean rainbowPrefix = true;

    public
    Managers ( ) {
        super ( "Management" , "ClientManagement" , Module.Category.CLIENT , false , true , true );
        this.setInstance ( );
    }

    public static
    Managers getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new Managers ( );
        }
        return INSTANCE;
    }

    private
    void setInstance ( ) {
        INSTANCE = this;
    }

    @Override
    public
    void onLoad ( ) {
        OyVey.commandManager.setClientMessage ( this.getCommandMessage ( ) );
    }


    public
    String getCommandMessage ( ) {

        return TextUtil.coloredString ( this.commandBracket , this.bracketColor ) + TextUtil.coloredString ( this.command , this.commandColor ) + TextUtil.coloredString ( this.commandBracket2 , this.bracketColor );
    }

    public
    String getRawCommandMessage ( ) {
        return this.commandBracket + this.command + this.commandBracket2;
    }

    public
    enum ThreadMode {
        POOL,
        WHILE,
        NONE
    }
}
