package me.derp.quantum.features.modules.movement;


import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlow extends Module {
    public Setting<Boolean> noSlow = register(new Setting<>("NoSlow", true));
    public Setting<Boolean> explosions = register(new Setting<>("Explosions", false));
    public Setting<Float> horizontal = this.register(new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f, v -> this.explosions.getValue()));
    public Setting<Float> vertical = this.register(new Setting<>("Vertical", 0.0f, 0.0f, 100.0f, v -> this.explosions.getValue()));
    private static NoSlow INSTANCE = new NoSlow();
    private final boolean sneaking = false;
    private static final KeyBinding[] keys = new KeyBinding[]{NoSlow.mc.gameSettings.keyBindForward, NoSlow.mc.gameSettings.keyBindBack, NoSlow.mc.gameSettings.keyBindLeft, NoSlow.mc.gameSettings.keyBindRight, NoSlow.mc.gameSettings.keyBindJump, NoSlow.mc.gameSettings.keyBindSprint};

    public NoSlow() {
        super("NoSlow", "Prevents you from getting slowed down.", Module.Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static NoSlow getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoSlow();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if (event.getStage() == 0 && mc.player != null) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                SPacketEntityVelocity velocity = event.getPacket();
                if (velocity.getEntityID() == mc.player.entityId) {
                    if (this.horizontal.getValue() == 0.0F && this.vertical.getValue() == 0.0F) {
                        event.setCanceled(true);
                        return;
                    }
                    velocity.motionX = (int) (velocity.motionX * this.horizontal.getValue());
                    velocity.motionY = (int) (velocity.motionY * this.vertical.getValue());
                    velocity.motionZ = (int) (velocity.motionZ * this.horizontal.getValue());
                }
            }
            if (this.explosions.getValue() && event.getPacket() instanceof SPacketExplosion) {
                SPacketExplosion velocity = event.getPacket();
                velocity.motionX *= this.horizontal.getValue();
                velocity.motionY *= this.vertical.getValue();
                velocity.motionZ *= this.horizontal.getValue();
            }
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (noSlow.getValue() && NoSlow.mc.player.isHandActive() && !NoSlow.mc.player.isRiding()) {
            event.getMovementInput().moveStrafe *= 5.0f;
            event.getMovementInput().moveForward *= 5.0f;
        }
    }
}