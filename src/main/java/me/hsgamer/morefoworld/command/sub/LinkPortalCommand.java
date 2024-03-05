package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import me.hsgamer.morefoworld.config.PortalConfig;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public class LinkPortalCommand extends LinkWorldCommand {
    private final MoreFoWorld plugin;

    public LinkPortalCommand(MoreFoWorld plugin) {
        super("linkportal", "Link portals between two worlds", 1, "<nether/end>", Permissions.LINK_PORTAL.getName());
        this.plugin = plugin;
    }

    @Override
    protected void onWorldCommand(CommandSender sender, World from, World to, String... args) {
        BiConsumer<String, String> action;
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "nether" -> action = plugin.get(PortalConfig.class)::linkNetherPortal;
            case "end" -> action = plugin.get(PortalConfig.class)::linkEndPortal;
            default -> {
                MessageUtils.sendMessage(sender, "&cInvalid type: &e" + args[0]);
                return;
            }
        }
        action.accept(from.getName(), to.getName());
        MessageUtils.sendMessage(sender, "&aSuccessfully linked");
    }

    @Override
    protected List<String> onWorldTabComplete(CommandSender sender, String... args) {
        if (args.length == 1) {
            return List.of("nether", "end");
        }
        return Collections.emptyList();
    }
}
