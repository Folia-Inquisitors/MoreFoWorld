package me.hsgamer.morefoworld.command.sub;

import io.github.projectunified.minelib.util.subcommand.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CurrentCommand extends SubCommand {
    public CurrentCommand() {
        super("current", "Get the world of the player", "/<label> current [player]", Permissions.CURRENT_WORLD.getName(), true);
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        Player player;
        if (args.length >= 1) {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                MessageUtils.sendMessage(sender, "&cThe player is not found");
                return;
            }

            if (player != sender && !sender.hasPermission(Permissions.CURRENT_WORLD_OTHERS.getName())) {
                MessageUtils.sendMessage(sender, "&cYou don't have permission to get the current world of other players");
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            MessageUtils.sendMessage(sender, "&cYou must be a player to use this command");
            return;
        }

        MessageUtils.sendMessage(sender, "&aThe current world of &e" + player.getName() + " &a is &e" + player.getWorld().getName());
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1 && sender.hasPermission(Permissions.TELEPORT_OTHERS)) {
            String arg = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(arg))
                    .toList();
        }
        return super.onTabComplete(sender, label, args);
    }
}
