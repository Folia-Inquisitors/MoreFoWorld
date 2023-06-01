package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class TeleportCommand extends SubCommand {
    private final MoreFoWorld plugin;

    public TeleportCommand(MoreFoWorld plugin) {
        super("teleport", "Teleport to a world", "/<label> teleport <world> [player]", Permissions.TELEPORT.getName(), true);
        this.plugin = plugin;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        Player player;
        if (args.length >= 2) {
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                MessageUtils.sendMessage(sender, "&cThe player is not found");
                return;
            }

            if (player != sender && !sender.hasPermission(Permissions.TELEPORT_OTHERS)) {
                MessageUtils.sendMessage(sender, "&cYou don't have permission to teleport other players");
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            MessageUtils.sendMessage(sender, "&cYou must be a player to use this command");
            return;
        }

        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            MessageUtils.sendMessage(sender, "&cThe world is not found");
            return;
        }

        MessageUtils.sendMessage(sender, "&aTeleporting...");
        player.teleportAsync(world.getSpawnLocation()).whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                MessageUtils.sendMessage(sender, "&cAn error occurred: " + throwable.getMessage());
                plugin.getLogger().log(Level.WARNING, "An error occurred while teleporting the player", throwable);
            } else {
                MessageUtils.sendMessage(sender, "&aYou have been teleported to &e" + world.getName());
            }
        });
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= 1;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            return Bukkit.getWorlds().stream()
                    .map(WorldInfo::getName)
                    .filter(name -> name.toLowerCase().startsWith(arg))
                    .toList();
        } else if (args.length == 2 && sender.hasPermission(Permissions.TELEPORT_OTHERS)) {
            String arg = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(arg))
                    .toList();
        }
        return Collections.emptyList();
    }
}
