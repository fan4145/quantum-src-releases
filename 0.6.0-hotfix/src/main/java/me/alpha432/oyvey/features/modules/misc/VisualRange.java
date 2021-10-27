package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class VisualRange extends Module {
    public Setting<Boolean> VisualRangeSound = this.register(new Setting<>("Sound", true));
    public Setting<Boolean> coords = this.register(new Setting<>("Coords", true));
    public Setting<Boolean> leaving = this.register(new Setting<>("leaving", true));


    private List<String> people;
    private List<EntityPlayer> knownPlayers = new ArrayList<>();

    public VisualRange() {
        super("VisualRange", "Visual range", Module.Category.CLIENT, true, false, false);
    }

    public void onEnable() {
        this.people = new ArrayList<>();
        this.knownPlayers = new ArrayList<>();
    }

    public void onUpdate() {
        ArrayList<EntityPlayer> tickPlayerList = new ArrayList<>(VisualRange.mc.world.playerEntities);
        if (tickPlayerList.size() > 0) {
            for (EntityPlayer player : tickPlayerList) {
                if (player.getName().equals(VisualRange.mc.player.getName()) || this.knownPlayers.contains(player))
                    continue;
                this.knownPlayers.add(player);
                if (OyVey.friendManager.isFriend(player)) {
                    Command.sendMessage("Player \u00a7a" + player.getName() + "\u00a7r" + " entered your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                } else {
                    Command.sendMessage("Player \u00a7c" + player.getName() + "\u00a7r" + " entered your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                }
                if (this.VisualRangeSound.getValue()) {
                    me.alpha432.oyvey.features.modules.misc.VisualRange.mc.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                }
                return;
            }
        }
        if (this.knownPlayers.size() > 0) {
            for (EntityPlayer player : this.knownPlayers) {
                if (tickPlayerList.contains(player)) continue;
                this.knownPlayers.remove(player);
                if (this.leaving.getValue()) {
                    if (OyVey.friendManager.isFriend(player)) {
                        Command.sendMessage("Player \u00a7a" + player.getName() + "\u00a7r" + " left your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                    } else {
                        Command.sendMessage("Player \u00a7c" + player.getName() + "\u00a7r" + " left your visual range" + (this.coords.getValue() ? " at (" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")!" : "!"));
                    }
                }
                return;
            }
        }


        if ((((mc.world == null) ? 1 : 0) | ((mc.player == null) ? 1 : 0)) != 0)
            return;
        List<String> peoplenew = new ArrayList<>();
        List<EntityPlayer> playerEntities = mc.world.playerEntities;
        for (Entity e : playerEntities) {
            if (e.getName().equals(mc.player.getName()))
                continue;
            peoplenew.add(e.getName());
        }
    }
}
