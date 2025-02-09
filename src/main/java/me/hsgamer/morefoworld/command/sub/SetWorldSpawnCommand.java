package me.hsgamer.morefoworld.command.sub;

import io.github.projectunified.minelib.util.subcommand.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import me.hsgamer.morefoworld.WorldUtil;
import me.hsgamer.morefoworld.config.WorldSpawnConfig;
import me.hsgamer.morefoworld.config.object.WorldPosition;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetWorldSpawnCommand extends SubCommand {
    private final MoreFoWorld plugin;

    public SetWorldSpawnCommand(MoreFoWorld plugin) {
        super("setworldspawn", "Set the spawn location of the world", "/<label> setworldspawn", Permissions.SET_WORLD_SPAWN.getName(), false);
        this.plugin = plugin;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        Player player = (Player) sender;
        WorldPosition worldPosition = WorldPosition.fromLocation(player.getLocation());
        plugin.get(WorldSpawnConfig.class).setSpawn(worldPosition);
        WorldUtil.applyWorldSpawn(worldPosition.toLocation());
        MessageUtils.sendMessage(player, "&aWorld spawn location set");
    }
}
