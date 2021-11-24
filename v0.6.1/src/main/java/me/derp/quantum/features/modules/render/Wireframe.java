package me.derp.quantum.features.modules.render;

import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Wireframe
        extends Module {
    private static Wireframe INSTANCE = new Wireframe();
    public final Setting<Float> alpha = this.register(new Setting<>("PAlpha", 255.0f, 0.1f, 255.0f));
    public final Setting<Float> lineWidth = this.register(new Setting<>("PLineWidth", 1.0f, 0.1f, 3.0f));
    public Setting<RenderMode> mode = this.register(new Setting<>("PMode", RenderMode.SOLID));
    public Setting<Boolean> players = this.register(new Setting<>("Players", Boolean.FALSE));
    public Setting<Boolean> playerModel = this.register(new Setting<>("PlayerModel", Boolean.FALSE));

    public Wireframe() {
        super("Wireframe", "Draws a wireframe esp around other players.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static Wireframe getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Wireframe();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderPlayerEvent(RenderPlayerEvent.Pre event) {
        event.getEntityPlayer().hurtTime = 0;
    }

    public enum RenderMode {
        SOLID,
        WIREFRAME

    }
}

