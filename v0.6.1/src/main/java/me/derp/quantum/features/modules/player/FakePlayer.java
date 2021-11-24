package me.derp.quantum.features.modules.player;

import com.mojang.authlib.GameProfile;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MoverType;

import java.util.Random;
import java.util.UUID;

public class FakePlayer extends Module {
    public Setting<Boolean> hollow = this.register(new Setting("Move", false));

    public FakePlayer() {
        super("FakePlayer", "Spawns fake player", Category.PLAYER, false, false, false);
    }

    private EntityOtherPlayerMP otherPlayer;

    public void onTick() {
        if (otherPlayer != null) {
            Random random = new Random();
            otherPlayer.moveForward = mc.player.moveForward + (random.nextInt(5) / 10F);
            otherPlayer.moveStrafing = mc.player.moveStrafing + (random.nextInt(5) / 10F);
            if (hollow.getValue()) travel(otherPlayer.moveStrafing, otherPlayer.moveVertical, otherPlayer.moveForward);
        }
    }

    public void travel(float strafe, float vertical, float forward) {
        double d0 = otherPlayer.posY;
        float f1 = 0.8F;
        float f2 = 0.02F;
        float f3 = (float) EnchantmentHelper.getDepthStriderModifier(otherPlayer);

        if (f3 > 3.0F) {
            f3 = 3.0F;
        }

        if (!otherPlayer.onGround) {
            f3 *= 0.5F;
        }

        if (f3 > 0.0F) {
            f1 += (0.54600006F - f1) * f3 / 3.0F;
            f2 += (otherPlayer.getAIMoveSpeed() - f2) * f3 / 4.0F;
        }

        otherPlayer.moveRelative(strafe, vertical, forward, f2);
        otherPlayer.move(MoverType.SELF, otherPlayer.motionX, otherPlayer.motionY, otherPlayer.motionZ);
        otherPlayer.motionX *= f1;
        otherPlayer.motionY *= 0.800000011920929D;
        otherPlayer.motionZ *= f1;

        if (!otherPlayer.hasNoGravity()) {
            otherPlayer.motionY -= 0.02D;
        }

        if (otherPlayer.collidedHorizontally && otherPlayer.isOffsetPositionInLiquid(otherPlayer.motionX, otherPlayer.motionY + 0.6000000238418579D - otherPlayer.posY + d0, otherPlayer.motionZ)) {
            otherPlayer.motionY = 0.30000001192092896D;
        }
    }

    @Override
    public void onEnable() {
        if (mc.world == null || mc.player == null) {
            toggle();
            return;
        }
        if (otherPlayer == null) {
            otherPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.randomUUID(), "TestDummy"));
            otherPlayer.copyLocationAndAnglesFrom(mc.player);
            otherPlayer.inventory.copyInventory(mc.player.inventory);
        }
        mc.world.spawnEntity(otherPlayer);

    }

    @Override
    public void onDisable() {
        if (otherPlayer != null) {
            mc.world.removeEntity(otherPlayer);
            otherPlayer = null;
        }
    }
}