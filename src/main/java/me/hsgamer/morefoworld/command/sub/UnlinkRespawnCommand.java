package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import me.hsgamer.morefoworld.config.RespawnConfig;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class UnlinkRespawnCommand extends UnlinkWorldCommand {
    private final MoreFoWorld plugin;

    public UnlinkRespawnCommand(MoreFoWorld plugin) {
        super("unlinkrespawn", "Unlink respawn location of a world", 0, "", Permissions.LINK_RESPAWN.getName());
        this.plugin = plugin;
    }

    @Override
    protected void onWorldCommand(CommandSender sender, World world, String... args) {
        if (plugin.get(RespawnConfig.class).unlinkWorld(world.getName())) {
            MessageUtils.sendMessage(sender, "&aSuccessfully unlinked");
        } else {
            MessageUtils.sendMessage(sender, "&cFailed to unlink. Is the respawn location already unlinked?");
        }
    }

    @Override
    protected List<String> onWorldTabComplete(CommandSender sender, String... args) {
        return Collections.emptyList();
    }
}
