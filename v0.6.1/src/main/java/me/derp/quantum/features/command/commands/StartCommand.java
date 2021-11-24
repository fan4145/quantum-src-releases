package me.derp.quantum.features.command.commands;

import me.derp.quantum.features.command.Command;


public class StartCommand
        extends Command {
    public StartCommand() {
        super("start", new String[]{"<number>"});
    }

    @Override
    public void execute(String[] var1) {

    }
}
