package net.paradisu.paradisuplugin.velocity.commands.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.velocitypowered.api.proxy.Player;

public class TeleportQueue {
    private static LoadingCache<TeleportRequestHeader, Player[]> teleportQueue = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(120, TimeUnit.SECONDS)
        .build(
            new CacheLoader<TeleportRequestHeader, Player[]>() {
                @Override
                public Player[] load(final TeleportRequestHeader player) throws Exception {
                    return null;
                }
    });

    private static LoadingCache<Player, Player> recentTeleportQueue = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(120, TimeUnit.SECONDS)
    .build(
        new CacheLoader<Player, Player>() {
            @Override
            public Player load(final Player player) throws Exception {
                return null;
            }
    });    

    public void queueTeleport(TeleportRequestHeader requestHeader, Player[] target) {
        teleportQueue.put(requestHeader, target);
        recentTeleportQueue.put(requestHeader.getRequestee(), requestHeader.getRequestor());
    }

    public void removeRecentTeleport(Player player) {
        recentTeleportQueue.invalidate(player);
    }
    
    public void removeTeleport(TeleportRequestHeader requestHeader) {
        teleportQueue.invalidate(requestHeader);
    }

    public boolean isTeleportQueued(TeleportRequestHeader requestHeader) {
        return teleportQueue.getIfPresent(requestHeader) != null;
    }

    public boolean isTeleportQueued(TeleportRequestHeader requestHeader, Player[] teleportArray) {
        return teleportQueue.getIfPresent(requestHeader) != null ? Arrays.equals(teleportArray, teleportQueue.getIfPresent(requestHeader)) : false;
    }

    public Player getRecentTeleport(Player player) {
        return recentTeleportQueue.getIfPresent(player);
    }

    public Player getPlayer(TeleportRequestHeader requestHeader, int i) {
        return teleportQueue.getIfPresent(requestHeader) == null ? null : (teleportQueue.getIfPresent(requestHeader)[i].isActive() ? teleportQueue.getIfPresent(requestHeader)[i] : null);
    }

}
