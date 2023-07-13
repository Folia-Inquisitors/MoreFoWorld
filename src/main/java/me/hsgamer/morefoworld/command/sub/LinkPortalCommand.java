package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public class LinkPortalCommand extends SubCommand {
    private final MoreFoWorld plugin;

    public LinkPortalCommand(MoreFoWorld plugin) {
        super("linkportal", "Link portals between two worlds", "/<label> linkportal <from> <to> <nether/end>", Permissions.LINK_PORTAL.getName(), true);
        this.plugin = plugin;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        World from = Bukkit.getWorld(args[0]);
        if (from == null) {
            MessageUtils.sendMessage(sender, "&cThe world &e" + args[1] + " &cis not found");
            return;
        }

        World to = Bukkit.getWorld(args[1]);
        if (to == null) {
            MessageUtils.sendMessage(sender, "&cThe world &e" + args[2] + " &cis not found");
            return;
        }

        if (from.equals(to)) {
            MessageUtils.sendMessage(sender, "&cThe world &e" + args[1] + " &cand &e" + args[2] + " &care the same");
            return;
        }

        BiConsumer<String, String> action;
        switch (args[2].toLowerCase(Locale.ROOT)) {
            case "nether" -> action = plugin.getPortalConfig()::linkNetherPortal;
            case "end" -> action = plugin.getPortalConfig()::linkEndPortal;
            default -> {
                MessageUtils.sendMessage(sender, "&cInvalid type: &e" + args[3]);
                return;
            }
        }
        action.accept(from.getName(), to.getName());
        MessageUtils.sendMessage(sender, "&aSuccessfully linked");
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= 3;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1 || args.length == 2) {
            return Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();
        } else if (args.length == 3) {
            return List.of("nether", "end");
        }
        return Collections.emptyList();
    }
}
