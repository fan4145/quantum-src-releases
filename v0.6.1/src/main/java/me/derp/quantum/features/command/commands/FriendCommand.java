package me.derp.quantum.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.derp.quantum.Quantum;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.misc.FriendSettings;
import me.derp.quantum.manager.FriendManager;
import me.derp.quantum.util.Util;
import net.minecraft.network.play.client.CPacketChatMessage;

public class FriendCommand
        extends Command {
    public FriendCommand() {
        super("friend", new String[]{"<add/del/name/clear>", "<name>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (Quantum.friendManager.getFriends().isEmpty()) {
                FriendCommand.sendMessage("Friend list empty D:.");
            } else {
                StringBuilder f = new StringBuilder("Friends: ");
                for (FriendManager.Friend friend : Quantum.friendManager.getFriends()) {
                    try {
                        f.append(friend.getUsername()).append(", ");
                    } catch (Exception ignored) {
                    }
                }
                FriendCommand.sendMessage(f.toString());
            }
            return;
        }
        if (commands.length == 2) {
            if ("reset".equals(commands[0])) {
                Quantum.friendManager.onLoad();
                FriendCommand.sendMessage("Friends got reset.");
                return;
            }
            FriendCommand.sendMessage(commands[0] + (Quantum.friendManager.isFriend(commands[0]) ? " is friended." : " isn't friended."));
            return;
        }
        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add": {
                    Quantum.friendManager.addFriend(commands[1]);
                    FriendCommand.sendMessage(ChatFormatting.GREEN + commands[1] + " has been friended");
                    if (FriendSettings.getInstance().notify.getValue()) {
                        Util.mc.player.connection.sendPacket(new CPacketChatMessage("/w " + commands[1] + " I just added you to my friends list on Quantum!"));
                    }
                    return;
                }
                case "del": {
                    Quantum.friendManager.removeFriend(commands[1]);
                    if (FriendSettings.getInstance().notify.getValue()) {
                        Util.mc.player.connection.sendPacket(new CPacketChatMessage("/w " + commands[1] + " I just removed you from my friends list on Quantum!"));
                    }
                    FriendCommand.sendMessage(ChatFormatting.RED + commands[1] + " has been unfriended");
                    return;
                }
            }
            FriendCommand.sendMessage("Unknown Command, try friend add/del (name)");
        }
    }
}

