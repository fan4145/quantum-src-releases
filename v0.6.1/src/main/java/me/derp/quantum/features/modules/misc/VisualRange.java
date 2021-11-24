package me.derp.quantum.features.modules.misc;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class VisualRange extends Module {
    public Setting<Boolean> VisualRangeSound = this.register(new Setting<>("Sound", true));
    public Setting<Boolean> coords = this.register(new Setting<>("Coords", true));
    public Setting<Boolean> leaving = this.register(new Setting<>("leaving", true));


    private List<EntityPlayer> knownPlayers = new ArrayList<>();

    public VisualRange() {
        super("VisualRange", "Visual range", Module.Category.CLIENT, true, false, false);
    }

    public void onEnable() {
        List <String> people = new ArrayList <>();
        this.knownPlayers = new ArrayList<>();
    }

    public void onUpdate() {
        ArrayList<EntityPlayer> tickPlayerList = new ArrayList<>(VisualRange.mc.world.playerEntities);
        if (tickPlayerList.size() > 0) {
            for (EntityPlayer player : tickPlayerList) {
                if (player.getName().equals(VisualRange.mc.player.getName()) || this.knownPlayers.contains(player))
                    continue;
                this.knownPlayers.add(player);
                if (Quantum.friendManager.isFriend(player)) {
                    Command.sendMessage("Player \u00a7a" + player.getName() + "\u00a7r" + " entered your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                } else {
                    Command.sendMessage("Player \u00a7c" + player.getName() + "\u00a7r" + " entered your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                }
                if (this.VisualRangeSound.getValue()) {
                    VisualRange.mc.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                }
                return;
            }
        }
        if (this.knownPlayers.size() > 0) {
            for (EntityPlayer player : this.knownPlayers) {
                if (tickPlayerList.contains(player)) continue;
                this.knownPlayers.remove(player);
                if (this.leaving.getValue()) {
                    if (Quantum.friendManager.isFriend(player)) {
                        Command.sendMessage("Player \u00a7a" + player.getName() + "\u00a7r" + " left your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                    } else {
                        Command.sendMessage("Player \u00a7c" + player.getName() + "\u00a7r" + " left your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                    }
                }
                return;
            }
        }


        if ((((Util.mc.world == null) ? 1 : 0) | ((Util.mc.player == null) ? 1 : 0)) != 0)
            return;
        List<String> peoplenew = new ArrayList<>();
        List<EntityPlayer> playerEntities = Util.mc.world.playerEntities;
        for (Entity e : playerEntities) {
            if (e.getName().equals(Util.mc.player.getName()))
                continue;
            peoplenew.add(e.getName());
        }
    }
}
