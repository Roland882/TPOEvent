package me.roland882.tpoevent.commands;

import me.roland882.tpoevent.TPOEvent;
import me.roland882.tpoevent.managers.MinionManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MinionCommand implements CommandExecutor {
    public TPOEvent plugin;
    public MiniMessage mm = MiniMessage.miniMessage();
    public MinionManager mmm;

    public MinionCommand(TPOEvent plugin, MinionManager mmm) {
        this.plugin = plugin;
        this.mmm = mmm;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<bold><gray>[<gold>TPO <dark_red>SMP<gray>]<reset> <red>Only players can use this command!"));
            return false;
        }

        if (!player.getName().equalsIgnoreCase("Roland882")) {
            player.sendMessage(mm.deserialize("<bold><gray>[<gold>TPO <dark_red>SMP<gray>]<reset> <red>You don't have access to this command!"));
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(mm.deserialize("<bold><gray>[<gold>TPO <dark_red>SMP<gray>]<reset> <red>Usage: /mmm <spawn|stop|despawn>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "spawn" -> {
                Location loc = player.getLocation();
                Vector dir = loc.getDirection().setY(0).normalize();
                Vector right = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
                double spacing = 2.0;
                double backRowOffset = 3.0;
                double sspacing = 2.0;
                Location f1 = loc.clone().add(right.clone().multiply(spacing / 2));
                Location f2 = loc.clone().subtract(right.clone().multiply(spacing / 2));
                mmm.spawnMin(f1, "§2Grinch Minion§f", player);
                mmm.spawnMin(f2, "§2Grinch Lackey§f", player);

                String[] backN = {"§2Grinch Henchman§f", "§2Grinch Sidekick§f", "§2Grinch Scout§f", "§2Grinch Enforcer§f"};
                for (int i = 0; i < 4; i++) {
                    double offset = (i - 1.5) * sspacing;
                    Location back = loc.clone()
                            .subtract(dir.clone().multiply(backRowOffset))
                            .add(right.clone().multiply(offset));
                    mmm.spawnMin(back, backN[i], player);
                }
                player.getInventory().addItem(mmm.ahorn());
                player.sendMessage(mm.deserialize("<bold><gray>[<gold>TPO <dark_red>SMP<gray>]<reset> <green>Helpers spawned! <dark_red><bold>Don't forget to set them up before the event!"));
                break;
            }
            case "stop" -> {
                mmm.setAttack(false);
                player.sendMessage(mm.deserialize("<bold><gray>[<gold>TPO <dark_red>SMP<gray>]<reset> <green>Helpers stopped and protected!"));
                break;
            }
            case "despawn" -> {
                mmm.desAllMin();
                player.sendMessage(mm.deserialize("<bold><gray>[<gold>TPO <dark_red>SMP<gray>]<reset> <green>Helpers despawned"));
                break;
            }
            default -> player.sendMessage(mm.deserialize("<bold><gray>[<gold>TPO <dark_red>SMP<gray>]<reset> <red>Unknown argument! Usage: /mmm <spawn|stop|despawn>"));
        }

        return true;
    }
}