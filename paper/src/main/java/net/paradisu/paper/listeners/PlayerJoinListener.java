package net.paradisu.paper.listeners;

import lombok.AllArgsConstructor;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.sync.PlayerJoinSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public class PlayerJoinListener implements Listener {
    private final ParadisuPaper paradisu;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(paradisu, new PlayerJoinSync(paradisu, player));
    }
}
