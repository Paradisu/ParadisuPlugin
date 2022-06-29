package net.paradisu.paradisuplugin.velocity.commands.util;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.velocitypowered.api.proxy.Player;

public class TeleportQueue {
    private static LoadingCache<Player, Player[]> teleportQueue = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(120, TimeUnit.SECONDS)
        .build(
            new CacheLoader<Player, Player[]>() {
                @Override
                public Player[] load(final Player player) throws Exception {
                    return null;
                }
        });

    public void queueTeleport(Player player, Player[] target) {
        teleportQueue.put(player, target);
    }

    public void removeTeleport(Player player) {
        teleportQueue.invalidate(player);
    }

    public Player getPlayer(Player player, int i) {
        return teleportQueue.getIfPresent(player) == null ? null : (teleportQueue.getIfPresent(player)[i].isActive() ? teleportQueue.getIfPresent(player)[i] : null);
    }
}
