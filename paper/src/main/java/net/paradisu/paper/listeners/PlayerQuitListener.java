package net.paradisu.paper.listeners;

import lombok.AllArgsConstructor;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.sync.PlayerQuitSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class PlayerQuitListener implements Listener {
    private final ParadisuPaper paradisu;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(paradisu, new PlayerQuitSync(paradisu, player));
    }
}
