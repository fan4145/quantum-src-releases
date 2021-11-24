package me.derp.quantum.features.command.commands;

import me.derp.quantum.Quantum;
import me.derp.quantum.features.command.Command;

public class UnloadCommand
        extends Command {
    public UnloadCommand() {
        super("unload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Quantum.unload(true);
    }
}

