package me.hsgamer.morefoworld.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import me.hsgamer.morefoworld.MoreFoWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalListener implements Listener {
    private final MoreFoWorld plugin;

    public PortalListener(MoreFoWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onReady(EntityPortalReadyEvent event) {
        System.out.println("Portal ready: " + event.getEntity());
        System.out.println("World: " + event.getTargetWorld());
        System.out.println("Type: " + event.getPortalType());
    }

    @EventHandler
    public void onEntityTeleport(EntityPortalEvent event) {
        System.out.println("Portal teleport: " + event.getEntity());
        System.out.println("From: " + event.getFrom());
        System.out.println("To: " + event.getTo());
        System.out.println("Type: " + event.getPortalType());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerPortalEvent event) {
        System.out.println("Portal teleport: " + event.getPlayer());
        System.out.println("From: " + event.getFrom());
        System.out.println("To: " + event.getTo());
        System.out.println("Cause: " + event.getCause());
    }
}
