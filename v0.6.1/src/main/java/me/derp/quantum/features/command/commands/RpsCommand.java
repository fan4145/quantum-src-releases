package me.derp.quantum.features.command.commands;

import me.derp.quantum.features.command.Command;
import java.util.Random;

public class RpsCommand extends Command {
    public RpsCommand() {
        super("rps", new String[]{"rock, paper, scissors"});
    }

    @Override
    public void execute(final String[] commands) {
        if (commands.length == 1 || commands.length == 0) {
            sendMessage("RPS > specify type u monke");
            return;
        }
        Random rng = new Random();
        int rngNumber = rng.nextInt(3);
        String check = commands[0];
        if (check.equalsIgnoreCase("rock")) {
            String value = getValue(rngNumber);
            if (value.equalsIgnoreCase("Rock")) sendSilentMessage("RPS > u draw noob");
            if (value.equalsIgnoreCase("Paper")) sendSilentMessage("RPS > LOL, you lose. what a loser");
            if (value.equalsIgnoreCase("Scissors")) sendSilentMessage("RPS > u won :O");
        } else if (check.equalsIgnoreCase("paper")) {
            String value = getValue(rngNumber);
            if (value.equalsIgnoreCase("Paper")) sendSilentMessage("RPS > u draw noob");
            if (value.equalsIgnoreCase("Scissors")) sendSilentMessage("RPS > LOL, you lose. what a loser");
            if (value.equalsIgnoreCase("Rock")) sendSilentMessage("RPS > u won :O");
        } else if (check.equalsIgnoreCase("scissors")) {
            String value = getValue(rngNumber);
            if (value.equalsIgnoreCase("Scissors")) sendSilentMessage("RPS > u draw noob");
            if (value.equalsIgnoreCase("Rock")) sendSilentMessage("RPS > LOL, you lose. what a loser");
            if (value.equalsIgnoreCase("Paper")) sendSilentMessage("RPS > u won :O");
        } else {
            sendSilentMessage("RPS > lol use rock/paper/scissors");
        }

        }
    String getValue(int a) {
        if (a == 1) return "Rock";
        if (a == 2) return "Paper";
        return "Scissors";
    }
        }

//e
