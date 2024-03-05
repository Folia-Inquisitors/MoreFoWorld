package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import me.hsgamer.morefoworld.config.SpawnConfig;
import me.hsgamer.morefoworld.config.object.WorldPosition;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand extends SubCommand {
    private final MoreFoWorld plugin;

    public SetSpawnCommand(MoreFoWorld plugin) {
        super("setspawn", "Set the spawn location the player will be teleported to when they joins the server", "/<label> setspawn", Permissions.SET_SPAWN.getName(), false);
        this.plugin = plugin;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        Player player = (Player) sender;
        plugin.get(SpawnConfig.class).setPosition(WorldPosition.fromLocation(player.getLocation()));
        MessageUtils.sendMessage(player, "&aSpawn location set");
    }
}
