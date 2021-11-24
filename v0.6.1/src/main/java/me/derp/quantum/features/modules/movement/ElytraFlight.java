package me.derp.quantum.features.modules.movement;

import me.derp.quantum.Quantum;
import me.derp.quantum.event.events.MoveEvent;
import me.derp.quantum.event.events.PacketEvent;
import me.derp.quantum.event.events.UpdateWalkingPlayerEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.InventoryUtil;
import me.derp.quantum.util.MathUtil;
import me.derp.quantum.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class ElytraFlight extends Module {
    private static ElytraFlight INSTANCE = new ElytraFlight();

    private final Timer timer = new Timer();

    private final Timer bypassTimer = new Timer();

    public Setting<Mode> mode = register(new Setting("Mode", Mode.FLY));

    public Setting<Integer> devMode = register(new Setting("Type", 2, 1, 3, v -> (this.mode.getValue() == Mode.BYPASS || this.mode.getValue() == Mode.BETTER), "EventMode"));

    public Setting<Float> speed = register(new Setting("Speed", 1.0F, 0.0F, 10.0F, v -> (this.mode.getValue() != Mode.FLY && this.mode.getValue() != Mode.BOOST && this.mode.getValue() != Mode.BETTER && this.mode.getValue() != Mode.OHARE), "The Speed."));

    public Setting<Float> vSpeed = register(new Setting("VSpeed", 0.3F, 0.0F, 10.0F, v -> (this.mode.getValue() == Mode.BETTER || this.mode.getValue() == Mode.OHARE), "Vertical Speed"));

    public Setting<Float> hSpeed = register(new Setting("HSpeed", 1.0F, 0.0F, 10.0F, v -> (this.mode.getValue() == Mode.BETTER || this.mode.getValue() == Mode.OHARE), "Horizontal Speed"));

    public Setting<Float> glide = register(new Setting("Glide", 1.0E-4F, 0.0F, 0.2F, v -> (this.mode.getValue() == Mode.BETTER), "Glide Speed"));

    public Setting<Float> tooBeeSpeed = register(new Setting("TooBeeSpeed", 1.8000001F, 1.0F, 2.0F, v -> (this.mode.getValue() == Mode.TOOBEE), "Speed for flight on 2b2t"));

    public Setting<Boolean> autoStart = register(new Setting("AutoStart", Boolean.TRUE));

    public Setting<Boolean> disableInLiquid = register(new Setting("NoLiquid", Boolean.TRUE));

    public Setting<Boolean> infiniteDura = register(new Setting("InfiniteDura", Boolean.FALSE));

    public Setting<Boolean> noKick = register(new Setting("NoKick", Boolean.FALSE, v -> (this.mode.getValue() == Mode.PACKET)));

    public Setting<Boolean> allowUp = register(new Setting("AllowUp", Boolean.TRUE, v -> (this.mode.getValue() == Mode.BETTER)));

    public Setting<Boolean> lockPitch = register(new Setting("LockPitch", Boolean.FALSE));

    public Setting<Boolean> Firework = register(new Setting("firework", Boolean.FALSE));

    public Setting<Boolean> Silent = register(new Setting("silent", Boolean.FALSE));


    public Setting<Float> fast = this.register(new Setting <>("Speed", 2.0f, 0.1f, 5.0f, v -> (this.mode.getValue() == Mode.CONTROL)));
    public Setting<Float> fastYUp = this.register(new Setting <>("Speed", 2.0f, 0.1f, 5.0f, v -> (this.mode.getValue() == Mode.CONTROL)));
    public Setting<Float> fastYDown = this.register(new Setting <>("Speed", 2.0f, 0.1f, 5.0f, v -> (this.mode.getValue() == Mode.CONTROL)));
    public Setting<Float> fastX = this.register(new Setting <>("Speed", 2.0f, 0.1f, 5.0f, v -> (this.mode.getValue() == Mode.CONTROL)));
    public Setting<Float> fastZ = this.register(new Setting <>("Speed", 2.0f, 0.1f, 5.0f, v -> (this.mode.getValue() == Mode.CONTROL)));

    private boolean vertical;

    private Double posX;

    private Double flyHeight;

    private Double posZ;

    public ElytraFlight() {
        super("ElytraFlight", "Makes Elytra Flight better.", Module.Category.MOVEMENT, true, false, false);
        setInstance();
    }

    public static ElytraFlight getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ElytraFlight();
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public void onEnable() {
        if (this.mode.getValue() == Mode.BETTER && !this.autoStart.getValue() && this.devMode.getValue() == 1)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
        this.flyHeight = null;
        this.posX = null;
        this.posZ = null;
    }

    public String getDisplayInfo() {
        return this.mode.currentEnumName();
    }

    public void onUpdate() {
        if (this.mode.getValue() == Mode.BYPASS && this.devMode.getValue() == 1 && mc.player.isElytraFlying()) {
            mc.player.motionX = 0.0D;
            mc.player.motionY = -1.0E-4D;
            mc.player.motionZ = 0.0D;
            double forwardInput = mc.player.movementInput.moveForward;
            double strafeInput = mc.player.movementInput.moveStrafe;
            double[] result = forwardStrafeYaw(forwardInput, strafeInput, mc.player.rotationYaw);
            double forward = result[0];
            double strafe = result[1];
            double yaw = result[2];
            if (forwardInput != 0.0D || strafeInput != 0.0D) {
                mc.player.motionX = forward * this.speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0D)) + strafe * this.speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0D));
                mc.player.motionZ = forward * this.speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0D)) - strafe * this.speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0D));
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown())
                mc.player.motionY = -1.0D;
        }
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && this.mode.getValue() == Mode.TOOBEE) {
            CPacketPlayer packet = event.getPacket();
            if (mc.player.isElytraFlying()) ;
        }
        if (event.getPacket() instanceof CPacketPlayer && this.mode.getValue() == Mode.TOOBEEBYPASS) {
            CPacketPlayer packet = event.getPacket();
            if (mc.player.isElytraFlying()) ;
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (this.mode.getValue() == Mode.OHARE) {
            ItemStack itemstack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack) && mc.player.isElytraFlying()) {
                event.setY(mc.gameSettings.keyBindJump.isKeyDown() ? this.vSpeed.getValue() : (mc.gameSettings.keyBindSneak.isKeyDown() ? -this.vSpeed.getValue() : 0.0D));
                mc.player.addVelocity(0.0D, mc.gameSettings.keyBindJump.isKeyDown() ? this.vSpeed.getValue() : (mc.gameSettings.keyBindSneak.isKeyDown() ? -this.vSpeed.getValue() : 0.0D), 0.0D);
                mc.player.rotateElytraX = 0.0F;
                mc.player.rotateElytraY = 0.0F;
                mc.player.rotateElytraZ = 0.0F;
                mc.player.moveVertical = mc.gameSettings.keyBindJump.isKeyDown() ? this.vSpeed.getValue() : (mc.gameSettings.keyBindSneak.isKeyDown() ? -this.vSpeed.getValue() : 0.0F);
                double forward = mc.player.movementInput.moveForward;
                double strafe = mc.player.movementInput.moveStrafe;
                float yaw = mc.player.rotationYaw;
                if (forward == 0.0D && strafe == 0.0D) {
                    event.setX(0.0D);
                    event.setZ(0.0D);
                } else {
                    if (forward != 0.0D) {
                        if (strafe > 0.0D) {
                            yaw += ((forward > 0.0D) ? -45 : 45);
                        } else if (strafe < 0.0D) {
                            yaw += ((forward > 0.0D) ? 45 : -45);
                        }
                        strafe = 0.0D;
                        if (forward > 0.0D) {
                            forward = 1.0D;
                        } else if (forward < 0.0D) {
                            forward = -1.0D;
                        }
                    }
                    double cos = Math.cos(Math.toRadians((yaw + 90.0F)));
                    double sin = Math.sin(Math.toRadians((yaw + 90.0F)));
                    event.setX(forward * this.hSpeed.getValue() * cos + strafe * this.hSpeed.getValue() * sin);
                    event.setZ(forward * this.hSpeed.getValue() * sin - strafe * this.hSpeed.getValue() * cos);
                }
            }
        } else if (event.getStage() == 0 && this.mode.getValue() == Mode.BYPASS && this.devMode.getValue() == 3) {
            if (mc.player.isElytraFlying()) {
                event.setX(0.0D);
                event.setY(-1.0E-4D);
                event.setZ(0.0D);
                double forwardInput = mc.player.movementInput.moveForward;
                double strafeInput = mc.player.movementInput.moveStrafe;
                double[] result = forwardStrafeYaw(forwardInput, strafeInput, mc.player.rotationYaw);
                double forward = result[0];
                double strafe = result[1];
                double yaw = result[2];
                if (forwardInput != 0.0D || strafeInput != 0.0D) {
                    event.setX(forward * this.speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0D)) + strafe * this.speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0D)));
                    event.setY(forward * this.speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0D)) - strafe * this.speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0D)));
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    event.setY(-1.0D);
            }
        } else if (this.mode.getValue() == Mode.TOOBEE) {
            if (!mc.player.isElytraFlying())
                return;
            if (!mc.player.movementInput.jump) {
                if (mc.player.movementInput.sneak) {
                    mc.player.motionY = -(this.tooBeeSpeed.getValue() / 2.0F);
                    event.setY(-(this.speed.getValue() / 2.0F));
                } else if (event.getY() != -1.01E-4D) {
                    event.setY(-1.01E-4D);
                    mc.player.motionY = -1.01E-4D;
                }
            } else {
                return;
            }
            setMoveSpeed(event, this.tooBeeSpeed.getValue());
        } else if (this.mode.getValue() == Mode.TOOBEEBYPASS) {
            if (!mc.player.isElytraFlying())
                return;
            if (!mc.player.movementInput.jump) {
                if (this.lockPitch.getValue())
                    mc.player.rotationPitch = 4.0F;
            } else {
                return;
            }
            if (Quantum.speedManager.getSpeedKpH() > 180.0D)
                return;
            double yaw = Math.toRadians(mc.player.rotationYaw);
            mc.player.motionX -= mc.player.movementInput.moveForward * Math.sin(yaw) * 0.04D;
            mc.player.motionZ += mc.player.movementInput.moveForward * Math.cos(yaw) * 0.04D;
        } else if (this.mode.getValue() == Mode.CONTROL) {
            final double[] directionSpeed = MathUtil.directionSpeed(this.fast.getValue());
            mc.player.motionY = 0; // Prevent the player from slowly falling down
            mc.player.motionX = 0;
            mc.player.motionZ = 0;

            if (mc.player.movementInput.jump) {
                mc.player.motionY = (this.speed.getValue() / 2) * this.fastYUp.getValue();
            } else if (mc.player.movementInput.sneak) {
                mc.player.motionY = -(this.speed.getValue() / 2) * this.fastYDown.getValue();
            }
            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                mc.player.motionX = directionSpeed[0] * this.fastX.getValue();
                mc.player.motionZ = directionSpeed[1] * this.fastX.getValue();
            }

            event.setX(mc.player.motionX);
            event.setY(mc.player.motionY);
            event.setZ(mc.player.motionZ);
        }
    }

    private void setMoveSpeed(MoveEvent event, double speed) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        if (forward == 0.0D && strafe == 0.0D) {
            event.setX(0.0D);
            event.setZ(0.0D);
            mc.player.motionX = 0.0D;
            mc.player.motionZ = 0.0D;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += ((forward > 0.0D) ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += ((forward > 0.0D) ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1.0D;
                } else if (forward < 0.0D) {
                    forward = -1.0D;
                }
            }
            double x = forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw));
            double z = forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw));
            event.setX(x);
            event.setZ(z);
            mc.player.motionX = x;
            mc.player.motionZ = z;
        }
    }

    public void onTick() {
        float yaw;
        if (!mc.player.isElytraFlying())
            return;
        switch (this.mode.getValue()) {
            case BOOST:
                if (mc.player.isInWater()) {
                    Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY += 0.08D;
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY -= 0.04D;
                }
                if (mc.gameSettings.keyBindForward.isKeyDown()) {
                    float f = (float) Math.toRadians(mc.player.rotationYaw);
                    mc.player.motionX -= (MathHelper.sin(f) * 0.05F);
                    mc.player.motionZ += (MathHelper.cos(f) * 0.05F);
                    break;
                }
                if (!mc.gameSettings.keyBindBack.isKeyDown())
                    break;
                yaw = (float) Math.toRadians(mc.player.rotationYaw);
                mc.player.motionX += (MathHelper.sin(yaw) * 0.05F);
                mc.player.motionZ -= (MathHelper.cos(yaw) * 0.05F);
                break;
            case FLY:
                mc.player.capabilities.isFlying = true;
                break;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        double rotationYaw;
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;
        switch (event.getStage()) {
            case 0:
                if (this.disableInLiquid.getValue() && (mc.player.isInWater() || mc.player.isInLava())) {
                    if (mc.player.isElytraFlying())
                        Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }
                if (this.autoStart.getValue() && mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isElytraFlying() && mc.player.motionY < 0.0D && this.timer.passedMs(250L)) {
                    Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    this.timer.reset();
                }
                if (this.mode.getValue() == Mode.BETTER) {
                    double[] dir = MathUtil.directionSpeed((this.devMode.getValue() == 1) ? this.speed.getValue() : this.hSpeed.getValue());
                    switch (this.devMode.getValue()) {
                        case 1:
                            mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                            mc.player.jumpMovementFactor = this.speed.getValue();
                            if (mc.gameSettings.keyBindJump.isKeyDown())
                                mc.player.motionY += this.speed.getValue();
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                mc.player.motionY -= this.speed.getValue();
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                mc.player.motionX = dir[0];
                                mc.player.motionZ = dir[1];
                                break;
                            }
                            mc.player.motionX = 0.0D;
                            mc.player.motionZ = 0.0D;
                            break;
                        case 2:
                            if (mc.player.isElytraFlying()) {
                                if (this.flyHeight == null)
                                    this.flyHeight = mc.player.posY;
                            } else {
                                this.flyHeight = null;
                                return;
                            }
                            if (this.noKick.getValue())
                                this.flyHeight = this.flyHeight - this.glide.getValue();
                            this.posX = 0.0D;
                            this.posZ = 0.0D;
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                this.posX = dir[0];
                                this.posZ = dir[1];
                            }
                            if (mc.gameSettings.keyBindJump.isKeyDown())
                                this.flyHeight = mc.player.posY + this.vSpeed.getValue();
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                this.flyHeight = mc.player.posY - this.vSpeed.getValue();
                            mc.player.setPosition(mc.player.posX + this.posX, this.flyHeight, mc.player.posZ + this.posZ);
                            mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                            break;
                        case 3:
                            if (mc.player.isElytraFlying()) {
                                if (this.flyHeight == null || this.posX == null || this.posX == 0.0D || this.posZ == null || this.posZ == 0.0D) {
                                    this.flyHeight = mc.player.posY;
                                    this.posX = mc.player.posX;
                                    this.posZ = mc.player.posZ;
                                }
                            } else {
                                this.flyHeight = null;
                                this.posX = null;
                                this.posZ = null;
                                return;
                            }
                            if (this.noKick.getValue())
                                this.flyHeight = this.flyHeight - this.glide.getValue();
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                this.posX = this.posX + dir[0];
                                this.posZ = this.posZ + dir[1];
                            }
                            if (this.allowUp.getValue() && mc.gameSettings.keyBindJump.isKeyDown())
                                this.flyHeight = mc.player.posY + (this.vSpeed.getValue() / 10.0F);
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                this.flyHeight = mc.player.posY - (this.vSpeed.getValue() / 10.0F);
                            mc.player.setPosition(this.posX, this.flyHeight, this.posZ);
                            mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                            break;
                    }
                }
                rotationYaw = Math.toRadians(mc.player.rotationYaw);
                if (mc.player.isElytraFlying()) {
                    float speedScaled;
                    double[] directionSpeedPacket;
                    double[] directionSpeedBypass;
                    switch (this.mode.getValue()) {
                        case VANILLA:
                            speedScaled = this.speed.getValue() * 0.05F;
                            if (mc.gameSettings.keyBindJump.isKeyDown())
                                mc.player.motionY += speedScaled;
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                mc.player.motionY -= speedScaled;
                            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                                mc.player.motionX -= Math.sin(rotationYaw) * speedScaled;
                                mc.player.motionZ += Math.cos(rotationYaw) * speedScaled;
                            }
                            if (!mc.gameSettings.keyBindBack.isKeyDown())
                                break;
                            mc.player.motionX += Math.sin(rotationYaw) * speedScaled;
                            mc.player.motionZ -= Math.cos(rotationYaw) * speedScaled;
                            break;
                        case PACKET:
                            freezePlayer(mc.player);
                            runNoKick(mc.player);
                            directionSpeedPacket = MathUtil.directionSpeed(this.speed.getValue());
                            if (mc.player.movementInput.jump)
                                mc.player.motionY = this.speed.getValue();
                            if (mc.player.movementInput.sneak)
                                mc.player.motionY = -this.speed.getValue();
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                mc.player.motionX = directionSpeedPacket[0];
                                mc.player.motionZ = directionSpeedPacket[1];
                            }
                            Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            break;
                        case BYPASS:
                            if (this.devMode.getValue() != 3)
                                break;
                            if (mc.gameSettings.keyBindJump.isKeyDown())
                                mc.player.motionY = 0.019999999552965164D;
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                mc.player.motionY = -0.20000000298023224D;
                            if (mc.player.ticksExisted % 8 == 0 && mc.player.posY <= 240.0D)
                                mc.player.motionY = 0.019999999552965164D;
                            mc.player.capabilities.isFlying = true;
                            mc.player.capabilities.setFlySpeed(0.025F);
                            directionSpeedBypass = MathUtil.directionSpeed(0.5199999809265137D);
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                mc.player.motionX = directionSpeedBypass[0];
                                mc.player.motionZ = directionSpeedBypass[1];
                                break;
                            }
                            mc.player.motionX = 0.0D;
                            mc.player.motionZ = 0.0D;
                            break;
                    }
                }
                if (!this.infiniteDura.getValue())
                    break;
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                break;
            case 1:
                if (!this.infiniteDura.getValue())
                    break;
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                break;
        }
    }

    private double[] forwardStrafeYaw(double forward, double strafe, double yaw) {
        double[] result = {forward, strafe, yaw};
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                result[2] = result[2] + ((forward > 0.0D) ? -45 : 45);
            } else if (strafe < 0.0D) {
                result[2] = result[2] + ((forward > 0.0D) ? 45 : -45);
            }
            result[1] = 0.0D;
            if (forward > 0.0D) {
                result[0] = 1.0D;
            } else if (forward < 0.0D) {
                result[0] = -1.0D;
            }
        }
        return result;
    }

    private void freezePlayer(EntityPlayer player) {
        player.motionX = 0.0D;
        player.motionY = 0.0D;
        player.motionZ = 0.0D;
    }

    private void runNoKick(EntityPlayer player) {
        if (this.noKick.getValue() && !player.isElytraFlying() && player.ticksExisted % 4 == 0)
            player.motionY = -0.03999999910593033D;
    }

    public void onDisable() {
        if (fullNullCheck() || mc.player.capabilities.isCreativeMode)
            return;
        mc.player.capabilities.isFlying = false;
    }

    public enum Mode {
        VANILLA, PACKET, BOOST, FLY, BYPASS, BETTER, OHARE, TOOBEE, TOOBEEBYPASS, CONTROL
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {

        if (nullCheck() && event.getPacket() instanceof SPacketPlayerPosLook && !Firework.getValue().equals(InventoryUtil.Switch.NONE)) {
            if (mc.player.isElytraFlying()) {
                InventoryUtil.switchToHotbarSlot(ItemFirework.class, Silent.getValue());
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }
    }





}
