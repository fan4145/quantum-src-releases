package me.derp.quantum.features.command.commands;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.command.Command;

public class ReloadCommand
        extends Command {
    public ReloadCommand() {
        super("reload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Quantum.reload();
    }
}

