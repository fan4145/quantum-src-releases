package me.derp.quantum.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.derp.quantum.Quantum;
import me.derp.quantum.features.command.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("Commands: ");
        for (Command command : Quantum.commandManager.getCommands()) {
            HelpCommand.sendMessage(ChatFormatting.GRAY + Quantum.commandManager.getPrefix() + command.getName());
        }
    }
}

