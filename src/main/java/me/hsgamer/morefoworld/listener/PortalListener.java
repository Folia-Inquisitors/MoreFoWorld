package me.hsgamer.morefoworld.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import me.hsgamer.morefoworld.MoreFoWorld;
import org.bukkit.PortalType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PortalListener implements Listener {
    private final MoreFoWorld plugin;

    public PortalListener(MoreFoWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortalReady(EntityPortalReadyEvent event) {
        if (event.getPortalType() != PortalType.NETHER) return;
        plugin.debug("Portal Ready: " + event.getEntity().getWorld());
        plugin.getPortalConfig().getWorldFromNetherPortal(event.getEntity().getWorld()).ifPresent(world -> {
            event.setTargetWorld(world);
            plugin.debug("Set Portal to " + world);
        });
    }
}
