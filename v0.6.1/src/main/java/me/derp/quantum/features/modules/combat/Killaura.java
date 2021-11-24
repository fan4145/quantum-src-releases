package me.derp.quantum.features.modules.combat;

import me.derp.quantum.Quantum;
import me.derp.quantum.event.events.UpdateWalkingPlayerEvent;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Killaura extends Module {
    public static Entity target;
    private final Timer timer = new Timer();
    public Setting<Float> range = register(new Setting("Range", 6.0F, 0.1F, 7.0F));
    public Setting<Boolean> delay = register(new Setting("HitDelay", Boolean.TRUE));
    public Setting<Boolean> rotate = register(new Setting("Rotate", Boolean.TRUE));
    public Setting<Boolean> onlySharp = register(new Setting("SwordOnly", Boolean.TRUE));
    public Setting<Float> raytrace = register(new Setting("Raytrace", 6.0F, 0.1F, 7.0F, "Wall Range."));
    public Setting<Boolean> players = register(new Setting("Players", Boolean.TRUE));
    public Setting<Boolean> mobs = register(new Setting("Mobs", Boolean.FALSE));
    public Setting<Boolean> animals = register(new Setting("Animals", Boolean.FALSE));
    public Setting<Boolean> vehicles = register(new Setting("Entities", Boolean.FALSE));
    public Setting<Boolean> projectiles = register(new Setting("Projectiles", Boolean.FALSE));
    public Setting<Boolean> tps = register(new Setting("TpsSync", Boolean.TRUE));
    public Setting<Boolean> packet = register(new Setting("Packet", Boolean.FALSE));

    public Killaura() {
        super("Killaura", "Kills aura.", Module.Category.COMBAT, true, false, false);
    }

    public void onTick() {
        if (!this.rotate.getValue())
            doKillaura();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 0 && this.rotate.getValue())
            doKillaura();
    }

    private void doKillaura() {
        if (this.onlySharp.getValue() && !EntityUtil.holdingWeapon(Util.mc.player)) {
            target = null;
            return;
        }
        int wait = !this.delay.getValue() ? 0 : (int) (DamageUtil.getCooldownByWeapon(Util.mc.player) * (this.tps.getValue() ? Quantum.serverManager.getTpsFactor() : 1.0F));
        if (!this.timer.passedMs(wait))
            return;
        target = getTarget();
        if (target == null)
            return;
        if (this.rotate.getValue())
            Quantum.rotationManager.lookAtEntity(target);
        EntityUtil.attackEntity(target, this.packet.getValue(), true);
        this.timer.reset();
    }

    private Entity getTarget() {
        Entity target = null;
        double distance = this.range.getValue();
        double maxHealth = 36.0D;
        for (EntityPlayer entity : Util.mc.world.playerEntities) {
            if (((!this.players.getValue() || !(entity instanceof EntityPlayer)) && (!this.animals.getValue() || !EntityUtil.isPassive(entity)) && (!this.mobs.getValue() || EntityUtil.isMobAggressive(entity)) && (!this.vehicles.getValue() || !EntityUtil.isVehicle(entity)) && (!this.projectiles.getValue() || !EntityUtil.isProjectile(entity))) || (entity instanceof net.minecraft.entity.EntityLivingBase &&
                    EntityUtil.isntValid(entity, distance)))
                continue;
            if (!Util.mc.player.canEntityBeSeen(entity) && !EntityUtil.canEntityFeetBeSeen(entity) && Util.mc.player.getDistanceSq(entity) > MathUtil.square(this.raytrace.getValue()))
                continue;
            if (target == null) {
                target = entity;
                distance = Util.mc.player.getDistanceSq(entity);
                maxHealth = EntityUtil.getHealth(entity);
                continue;
            }
            if (DamageUtil.isArmorLow(entity, 18)) {
                target = entity;
                break;
            }
            if (Util.mc.player.getDistanceSq(entity) < distance) {
                target = entity;
                distance = Util.mc.player.getDistanceSq(entity);
                maxHealth = EntityUtil.getHealth(entity);
            }
            if (EntityUtil.getHealth(entity) < maxHealth) {
                target = entity;
                distance = Util.mc.player.getDistanceSq(entity);
                maxHealth = EntityUtil.getHealth(entity);
            }
        }
        return target;
    }

    public String getDisplayInfo() {
        if (target instanceof EntityPlayer)
            return target.getName();
        return null;
    }
}
