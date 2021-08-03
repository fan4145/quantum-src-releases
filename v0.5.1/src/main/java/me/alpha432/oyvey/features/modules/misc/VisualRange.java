package me.alpha432.oyvey.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.List;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class VisualRange extends Module {
  private List<String> people;

  public VisualRange() {
    super("VisualRange", "Visual range", Module.Category.CLIENT, true, false, false);
  }

  public void onEnable() {
    this.people = new ArrayList<>();
  }

  public void onUpdate() {
    if ((((mc.world == null) ? 1 : 0) | ((mc.player == null) ? 1 : 0)) != 0)
      return;
    List<String> peoplenew = new ArrayList<>();
    List<EntityPlayer> playerEntities = mc.world.playerEntities;
    for (Entity e : playerEntities) {
      if (e.getName().equals(mc.player.getName()))
        continue;
      peoplenew.add(e.getName());
    }
    if (peoplenew.size() > 0)
      for (String name : peoplenew) {
        if (!this.people.contains(name)) {
          if (OyVey.friendManager.isFriend(name)) {
            Command.sendMessage("my swag friend, " + ChatFormatting.RESET + ChatFormatting.GREEN + name + ChatFormatting.RESET + " just entered my visual range");
          } else {
            Command.sendMessage( "hey, a stalker name " + ChatFormatting.RESET + ChatFormatting.RED + ChatFormatting.BOLD + name + ChatFormatting.RESET + " just entered your visual range");
          }
          this.people.add(name);
        }
      }
  }
}
