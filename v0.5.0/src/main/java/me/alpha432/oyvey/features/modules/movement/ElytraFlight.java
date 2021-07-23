package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.MoveEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.UpdateWalkingPlayerEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.MathUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ElytraFlight extends Module {
    private static ElytraFlight INSTANCE = new ElytraFlight();

    private final Timer timer = new Timer();

    private final Timer bypassTimer = new Timer();

    public Setting<Mode> mode = register(new Setting("Mode", Mode.FLY));

    public Setting<Integer> devMode = register(new Setting("Type", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(3), v -> (this.mode.getValue() == Mode.BYPASS || this.mode.getValue() == Mode.BETTER), "EventMode"));

    public Setting<Float> speed = register(new Setting("Speed", Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(10.0F), v -> (this.mode.getValue() != Mode.FLY && this.mode.getValue() != Mode.BOOST && this.mode.getValue() != Mode.BETTER && this.mode.getValue() != Mode.OHARE), "The Speed."));

    public Setting<Float> vSpeed = register(new Setting("VSpeed", Float.valueOf(0.3F), Float.valueOf(0.0F), Float.valueOf(10.0F), v -> (this.mode.getValue() == Mode.BETTER || this.mode.getValue() == Mode.OHARE), "Vertical Speed"));

    public Setting<Float> hSpeed = register(new Setting("HSpeed", Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(10.0F), v -> (this.mode.getValue() == Mode.BETTER || this.mode.getValue() == Mode.OHARE), "Horizontal Speed"));

    public Setting<Float> glide = register(new Setting("Glide", Float.valueOf(1.0E-4F), Float.valueOf(0.0F), Float.valueOf(0.2F), v -> (this.mode.getValue() == Mode.BETTER), "Glide Speed"));

    public Setting<Float> tooBeeSpeed = register(new Setting("TooBeeSpeed", Float.valueOf(1.8000001F), Float.valueOf(1.0F), Float.valueOf(2.0F), v -> (this.mode.getValue() == Mode.TOOBEE), "Speed for flight on 2b2t"));

    public Setting<Boolean> autoStart = register(new Setting("AutoStart", Boolean.valueOf(true)));

    public Setting<Boolean> disableInLiquid = register(new Setting("NoLiquid", Boolean.valueOf(true)));

    public Setting<Boolean> infiniteDura = register(new Setting("InfiniteDura", Boolean.valueOf(false)));

    public Setting<Boolean> noKick = register(new Setting("NoKick", Boolean.valueOf(false), v -> (this.mode.getValue() == Mode.PACKET)));

    public Setting<Boolean> allowUp = register(new Setting("AllowUp", Boolean.valueOf(true), v -> (this.mode.getValue() == Mode.BETTER)));

    public Setting<Boolean> lockPitch = register(new Setting("LockPitch", Boolean.valueOf(false)));

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
        if (this.mode.getValue() == Mode.BETTER && !((Boolean)this.autoStart.getValue()).booleanValue() && ((Integer)this.devMode.getValue()).intValue() == 1)
            mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
        this.flyHeight = null;
        this.posX = null;
        this.posZ = null;
    }

    public String getDisplayInfo() {
        return this.mode.currentEnumName();
    }

    public void onUpdate() {
        if (this.mode.getValue() == Mode.BYPASS && ((Integer)this.devMode.getValue()).intValue() == 1 && mc.player.isElytraFlying()) {
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
                mc.player.motionX = forward * ((Float)this.speed.getValue()).floatValue() * Math.cos(Math.toRadians(yaw + 90.0D)) + strafe * ((Float)this.speed.getValue()).floatValue() * Math.sin(Math.toRadians(yaw + 90.0D));
                mc.player.motionZ = forward * ((Float)this.speed.getValue()).floatValue() * Math.sin(Math.toRadians(yaw + 90.0D)) - strafe * ((Float)this.speed.getValue()).floatValue() * Math.cos(Math.toRadians(yaw + 90.0D));
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown())
                mc.player.motionY = -1.0D;
        }
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && this.mode.getValue() == Mode.TOOBEE) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            if (mc.player.isElytraFlying());
        }
        if (event.getPacket() instanceof CPacketPlayer && this.mode.getValue() == Mode.TOOBEEBYPASS) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            if (mc.player.isElytraFlying());
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (this.mode.getValue() == Mode.OHARE) {
            ItemStack itemstack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack) && mc.player.isElytraFlying()) {
                event.setY(mc.gameSettings.keyBindJump.isKeyDown() ? ((Float)this.vSpeed.getValue()).floatValue() : (mc.gameSettings.keyBindSneak.isKeyDown() ? -((Float)this.vSpeed.getValue()).floatValue() : 0.0D));
                mc.player.addVelocity(0.0D, mc.gameSettings.keyBindJump.isKeyDown() ? ((Float)this.vSpeed.getValue()).floatValue() : (mc.gameSettings.keyBindSneak.isKeyDown() ? -((Float)this.vSpeed.getValue()).floatValue() : 0.0D), 0.0D);
                mc.player.rotateElytraX = 0.0F;
                mc.player.rotateElytraY = 0.0F;
                mc.player.rotateElytraZ = 0.0F;
                mc.player.moveVertical = mc.gameSettings.keyBindJump.isKeyDown() ? ((Float)this.vSpeed.getValue()).floatValue() : (mc.gameSettings.keyBindSneak.isKeyDown() ? -((Float)this.vSpeed.getValue()).floatValue() : 0.0F);
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
                    event.setX(forward * ((Float)this.hSpeed.getValue()).floatValue() * cos + strafe * ((Float)this.hSpeed.getValue()).floatValue() * sin);
                    event.setZ(forward * ((Float)this.hSpeed.getValue()).floatValue() * sin - strafe * ((Float)this.hSpeed.getValue()).floatValue() * cos);
                }
            }
        } else if (event.getStage() == 0 && this.mode.getValue() == Mode.BYPASS && ((Integer)this.devMode.getValue()).intValue() == 3) {
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
                    event.setX(forward * ((Float)this.speed.getValue()).floatValue() * Math.cos(Math.toRadians(yaw + 90.0D)) + strafe * ((Float)this.speed.getValue()).floatValue() * Math.sin(Math.toRadians(yaw + 90.0D)));
                    event.setY(forward * ((Float)this.speed.getValue()).floatValue() * Math.sin(Math.toRadians(yaw + 90.0D)) - strafe * ((Float)this.speed.getValue()).floatValue() * Math.cos(Math.toRadians(yaw + 90.0D)));
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    event.setY(-1.0D);
            }
        } else if (this.mode.getValue() == Mode.TOOBEE) {
            if (!mc.player.isElytraFlying())
                return;
            if (!mc.player.movementInput.jump) {
                if (mc.player.movementInput.sneak) {
                    mc.player.motionY = -(((Float)this.tooBeeSpeed.getValue()).floatValue() / 2.0F);
                    event.setY(-(((Float)this.speed.getValue()).floatValue() / 2.0F));
                } else if (event.getY() != -1.01E-4D) {
                    event.setY(-1.01E-4D);
                    mc.player.motionY = -1.01E-4D;
                }
            } else {
                return;
            }
            setMoveSpeed(event, ((Float)this.tooBeeSpeed.getValue()).floatValue());
        } else if (this.mode.getValue() == Mode.TOOBEEBYPASS) {
            if (!mc.player.isElytraFlying())
                return;
            if (!mc.player.movementInput.jump) {
                if (((Boolean)this.lockPitch.getValue()).booleanValue())
                    mc.player.rotationPitch = 4.0F;
            } else {
                return;
            }
            if (OyVey.speedManager.getSpeedKpH() > 180.0D)
                return;
            double yaw = Math.toRadians(mc.player.rotationYaw);
            mc.player.motionX -= mc.player.movementInput.moveForward * Math.sin(yaw) * 0.04D;
            mc.player.motionZ += mc.player.movementInput.moveForward * Math.cos(yaw) * 0.04D;
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
        switch ((Mode)this.mode.getValue()) {
            case BOOST:
                if (mc.player.isInWater()) {
                    mc.getConnection().sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY += 0.08D;
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY -= 0.04D;
                }
                if (mc.gameSettings.keyBindForward.isKeyDown()) {
                    float f = (float)Math.toRadians(mc.player.rotationYaw);
                    mc.player.motionX -= (MathHelper.sin(f) * 0.05F);
                    mc.player.motionZ += (MathHelper.cos(f) * 0.05F);
                    break;
                }
                if (!mc.gameSettings.keyBindBack.isKeyDown())
                    break;
                yaw = (float)Math.toRadians(mc.player.rotationYaw);
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
                if (((Boolean)this.disableInLiquid.getValue()).booleanValue() && (mc.player.isInWater() || mc.player.isInLava())) {
                    if (mc.player.isElytraFlying())
                        mc.getConnection().sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }
                if (((Boolean)this.autoStart.getValue()).booleanValue() && mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isElytraFlying() && mc.player.motionY < 0.0D && this.timer.passedMs(250L)) {
                    mc.getConnection().sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    this.timer.reset();
                }
                if (this.mode.getValue() == Mode.BETTER) {
                    double[] dir = MathUtil.directionSpeed((((Integer)this.devMode.getValue()).intValue() == 1) ? ((Float)this.speed.getValue()).floatValue() : ((Float)this.hSpeed.getValue()).floatValue());
                    switch (((Integer)this.devMode.getValue()).intValue()) {
                        case 1:
                            mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                            mc.player.jumpMovementFactor = ((Float)this.speed.getValue()).floatValue();
                            if (mc.gameSettings.keyBindJump.isKeyDown())
                                mc.player.motionY += ((Float)this.speed.getValue()).floatValue();
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                mc.player.motionY -= ((Float)this.speed.getValue()).floatValue();
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
                                    this.flyHeight = Double.valueOf(mc.player.posY);
                            } else {
                                this.flyHeight = null;
                                return;
                            }
                            if (((Boolean)this.noKick.getValue()).booleanValue())
                                this.flyHeight = Double.valueOf(this.flyHeight.doubleValue() - ((Float)this.glide.getValue()).floatValue());
                            this.posX = Double.valueOf(0.0D);
                            this.posZ = Double.valueOf(0.0D);
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                this.posX = Double.valueOf(dir[0]);
                                this.posZ = Double.valueOf(dir[1]);
                            }
                            if (mc.gameSettings.keyBindJump.isKeyDown())
                                this.flyHeight = Double.valueOf(mc.player.posY + ((Float)this.vSpeed.getValue()).floatValue());
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                this.flyHeight = Double.valueOf(mc.player.posY - ((Float)this.vSpeed.getValue()).floatValue());
                            mc.player.setPosition(mc.player.posX + this.posX.doubleValue(), this.flyHeight.doubleValue(), mc.player.posZ + this.posZ.doubleValue());
                            mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                            break;
                        case 3:
                            if (mc.player.isElytraFlying()) {
                                if (this.flyHeight == null || this.posX == null || this.posX.doubleValue() == 0.0D || this.posZ == null || this.posZ.doubleValue() == 0.0D) {
                                    this.flyHeight = Double.valueOf(mc.player.posY);
                                    this.posX = Double.valueOf(mc.player.posX);
                                    this.posZ = Double.valueOf(mc.player.posZ);
                                }
                            } else {
                                this.flyHeight = null;
                                this.posX = null;
                                this.posZ = null;
                                return;
                            }
                            if (((Boolean)this.noKick.getValue()).booleanValue())
                                this.flyHeight = Double.valueOf(this.flyHeight.doubleValue() - ((Float)this.glide.getValue()).floatValue());
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                this.posX = Double.valueOf(this.posX.doubleValue() + dir[0]);
                                this.posZ = Double.valueOf(this.posZ.doubleValue() + dir[1]);
                            }
                            if (((Boolean)this.allowUp.getValue()).booleanValue() && mc.gameSettings.keyBindJump.isKeyDown())
                                this.flyHeight = Double.valueOf(mc.player.posY + (((Float)this.vSpeed.getValue()).floatValue() / 10.0F));
                            if (mc.gameSettings.keyBindSneak.isKeyDown())
                                this.flyHeight = Double.valueOf(mc.player.posY - (((Float)this.vSpeed.getValue()).floatValue() / 10.0F));
                            mc.player.setPosition(this.posX.doubleValue(), this.flyHeight.doubleValue(), this.posZ.doubleValue());
                            mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                            break;
                    }
                }
                rotationYaw = Math.toRadians(mc.player.rotationYaw);
                if (mc.player.isElytraFlying()) {
                    float speedScaled;
                    double[] directionSpeedPacket;
                    double[] directionSpeedBypass;
                    switch ((Mode)this.mode.getValue()) {
                        case VANILLA:
                            speedScaled = ((Float)this.speed.getValue()).floatValue() * 0.05F;
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
                            freezePlayer((EntityPlayer)mc.player);
                            runNoKick((EntityPlayer)mc.player);
                            directionSpeedPacket = MathUtil.directionSpeed(((Float)this.speed.getValue()).floatValue());
                            if (mc.player.movementInput.jump)
                                mc.player.motionY = ((Float)this.speed.getValue()).floatValue();
                            if (mc.player.movementInput.sneak)
                                mc.player.motionY = -((Float)this.speed.getValue()).floatValue();
                            if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                                mc.player.motionX = directionSpeedPacket[0];
                                mc.player.motionZ = directionSpeedPacket[1];
                            }
                            mc.getConnection().sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            mc.getConnection().sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            break;
                        case BYPASS:
                            if (((Integer)this.devMode.getValue()).intValue() != 3)
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
                if (!((Boolean)this.infiniteDura.getValue()).booleanValue())
                    break;
                mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                break;
            case 1:
                if (!((Boolean)this.infiniteDura.getValue()).booleanValue())
                    break;
                mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                break;
        }
    }

    private double[] forwardStrafeYaw(double forward, double strafe, double yaw) {
        double[] result = { forward, strafe, yaw };
        if ((forward != 0.0D || strafe != 0.0D) && forward != 0.0D) {
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
        if (((Boolean)this.noKick.getValue()).booleanValue() && !player.isElytraFlying() && player.ticksExisted % 4 == 0)
            player.motionY = -0.03999999910593033D;
    }

    public void onDisable() {
        if (fullNullCheck() || mc.player.capabilities.isCreativeMode)
            return;
        mc.player.capabilities.isFlying = false;
    }

    public enum Mode {
        VANILLA, PACKET, BOOST, FLY, BYPASS, BETTER, OHARE, TOOBEE, TOOBEEBYPASS;
    }
}
