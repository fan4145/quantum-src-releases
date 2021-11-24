package me.derp.quantum.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.modules.client.HUD;
import me.derp.quantum.features.modules.client.ModuleTools;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;

public class PopCounter
        extends Module {
    public static HashMap<String, Integer> TotemPopContainer = new HashMap<>();
    private static PopCounter INSTANCE = new PopCounter();


    public PopCounter() {
        super("PopCounter", "Counts other players totem pops.", Module.Category.MISC, true, false, false);
        this.setInstance();
    }

    public static PopCounter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PopCounter();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        TotemPopContainer.clear();
    }

    public String death1(EntityPlayer player) {
        int l_Count = TotemPopContainer.get(player.getName());
        TotemPopContainer.remove(player.getName());
        if (l_Count == 1) {
            if (ModuleTools.getInstance().isEnabled()) {
                switch (ModuleTools.getInstance().popNotifier.getValue()) {
                    case FUTURE: {
                        return ChatFormatting.RED + "[Future] " + ChatFormatting.GREEN + player.getName() + ChatFormatting.GRAY + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.GRAY + " totem.";
                    }
                    case PHOBOS: {
                        return ChatFormatting.GOLD + player.getName() + ChatFormatting.RED + " died after popping " + ChatFormatting.GOLD + l_Count + ChatFormatting.RED + " totem.";
                    }
                    case DOTGOD: {
                        return ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "DotGod.CC" + ChatFormatting.DARK_PURPLE + "] " + ChatFormatting.LIGHT_PURPLE + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.LIGHT_PURPLE + " time!";
                    }
                    case NONE: {
                        return HUD.getInstance().getCommandMessage() + ChatFormatting.WHITE + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totem!";

                    }
                }
            } else {
                return HUD.getInstance().getCommandMessage() + ChatFormatting.WHITE + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totem!";

            }
        } else {
            if (ModuleTools.getInstance().isEnabled()) {
                switch (ModuleTools.getInstance().popNotifier.getValue()) {
                    case FUTURE: {
                        return ChatFormatting.RED + "[Future] " + ChatFormatting.GREEN + player.getName() + ChatFormatting.GRAY + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.GRAY + " totems.";
                    }
                    case PHOBOS: {
                        return ChatFormatting.GOLD + player.getName() + ChatFormatting.RED + " died after popping " + ChatFormatting.GOLD + l_Count + ChatFormatting.RED + " totems.";
                    }
                    case DOTGOD: {
                        return ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "DotGod.CC" + ChatFormatting.DARK_PURPLE + "] " + ChatFormatting.LIGHT_PURPLE + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.LIGHT_PURPLE + " times!";
                    }
                    case NONE: {
                        return HUD.getInstance().getCommandMessage() + ChatFormatting.WHITE + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totems!";

                    }
                }
            } else {
                return HUD.getInstance().getCommandMessage() + ChatFormatting.WHITE + player.getName() + " died after popping " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totems!";
            }
        }
        return null;
    }

    public void onDeath(EntityPlayer player) {
        if (PopCounter.fullNullCheck()) {
            return;
        }
        if (getInstance().isDisabled())
            return;
        if (PopCounter.mc.player.equals(player)) {
            return;
        }
        if (TotemPopContainer.containsKey(player.getName())) {
            Command.sendSilentMessage(death1(player));
        }
    }

    public String pop(EntityPlayer player) {
        int l_Count = 1;
        if (TotemPopContainer.containsKey(player.getName())) {
            l_Count = TotemPopContainer.get(player.getName());
            TotemPopContainer.put(player.getName(), ++l_Count);
        } else {
            TotemPopContainer.put(player.getName(), l_Count);
        }
        if (l_Count == 1) {
            if (ModuleTools.getInstance().isEnabled()) {
                switch (ModuleTools.getInstance().popNotifier.getValue()) {
                    case FUTURE: {
                        return ChatFormatting.RED + "[Future] " + ChatFormatting.GREEN + player.getName() + ChatFormatting.GRAY + " just popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.GRAY + " totem.";
                    }
                    case PHOBOS: {
                        return ChatFormatting.GOLD + player.getName() + ChatFormatting.RED + " popped " + ChatFormatting.GOLD + l_Count + ChatFormatting.RED + " totem.";
                    }
                    case DOTGOD: {
                        return ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "DotGod.CC" + ChatFormatting.DARK_PURPLE + "] " + ChatFormatting.LIGHT_PURPLE + player.getName() + " has popped " + ChatFormatting.RED + l_Count + ChatFormatting.LIGHT_PURPLE + " time in total!";
                    }
                    case NONE: {
                        return HUD.getInstance().getCommandMessage() + ChatFormatting.WHITE + player.getName() + " popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totem.";
                    }
                }
            } else {
                return HUD.getInstance().getCommandMessage() + ChatFormatting.WHITE + player.getName() + " popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totem.";
            }
        } else {
            if (ModuleTools.getInstance().isEnabled()) {
                switch (ModuleTools.getInstance().popNotifier.getValue()) {
                    case FUTURE: {
                        return ChatFormatting.RED + "[Future] " + ChatFormatting.GREEN + player.getName() + ChatFormatting.GRAY + " just popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.GRAY + " totems.";
                    }
                    case PHOBOS: {
                        return ChatFormatting.GOLD + player.getName() + ChatFormatting.RED + " popped " + ChatFormatting.GOLD + l_Count + ChatFormatting.RED + " totems.";
                    }
                    case DOTGOD: {
                        return ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "DotGod.CC" + ChatFormatting.DARK_PURPLE + "] " + ChatFormatting.LIGHT_PURPLE + player.getName() + " has popped " + ChatFormatting.RED + l_Count + ChatFormatting.LIGHT_PURPLE + " times in total!";
                    }
                    case NONE: {
                        return ChatFormatting.WHITE + player.getName() + " popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totems.";
                    }
                }
            } else {
                return HUD.getInstance().getCommandMessage() + ChatFormatting.WHITE + player.getName() + " popped " + ChatFormatting.GREEN + l_Count + ChatFormatting.WHITE + " Totems.";
            }
        }
        return "";
    }

    public void onTotemPop(EntityPlayer player) {
        if (PopCounter.fullNullCheck()) {
            return;
        }
        if (getInstance().isDisabled())
            return;
        if (PopCounter.mc.player.equals(player)) {
            return;
        }
        Command.sendSilentMessage(pop(player));
    }
}