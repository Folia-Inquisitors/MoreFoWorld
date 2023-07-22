package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class LinkRespawnCommand extends LinkWorldCommand {
    private final MoreFoWorld plugin;

    public LinkRespawnCommand(MoreFoWorld plugin) {
        super("linkrespawn", "Link respawn location between two worlds", 0, "", Permissions.LINK_RESPAWN.getName());
        this.plugin = plugin;
    }

    @Override
    protected void onWorldCommand(CommandSender sender, World from, World to, String... args) {
        plugin.getRespawnConfig().linkWorld(from.getName(), to.getName());
        MessageUtils.sendMessage(sender, "&aSuccessfully linked");
    }

    @Override
    protected List<String> onWorldTabComplete(CommandSender sender, String... args) {
        return Collections.emptyList();
    }
}
