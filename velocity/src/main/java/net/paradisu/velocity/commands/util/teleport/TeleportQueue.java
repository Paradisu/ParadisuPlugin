package net.paradisu.velocity.commands.util.teleport;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.velocitypowered.api.proxy.Player;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

    /**
     * Adds a player to the recent teleport queue and the teleport queue
     * 
     * @param requestor The player who requested the teleport
     * @param requestee The player who is being teleported
     */
    public void queueTeleport(TeleportRequestHeader requestHeader, Player[] target) {
        teleportQueue.put(requestHeader, target);
        recentTeleportQueue.put(requestHeader.getRequestee(), requestHeader.getRequestor());
    }

    /**
     * Removes a player from the recent teleport queue
     * 
     * @param player The player who is being removed from the queue
     */
    public void removeRecentTeleport(Player player) {
        recentTeleportQueue.invalidate(player);
    }

    /**
     * Removes a TeleportRequestHeader from the recent teleport queue
     * 
     * @param requestHeader The TeleportRequestHeader that is being removed from the
     *                      queue
     */
    public void removeTeleport(TeleportRequestHeader requestHeader) {
        teleportQueue.invalidate(requestHeader);
    }

    /**
     * Checks if a TeleportRequestHeader is in the recent teleport queue
     * 
     * @param requestHeader The TeleportRequestHeader to be checked
     * @return True if the TeleportRequestHeader is in the recent teleport queue,
     *         false otherwise
     */
    public boolean isTeleportQueued(TeleportRequestHeader requestHeader) {
        return teleportQueue.getIfPresent(requestHeader) != null;
    }

    /**
     * Checks if a TeleportRequestHeader eith a given player array is in the recent
     * teleport queue
     * 
     * @param requestHeader The TeleportRequestHeader to be checked
     * @param teleportArray The player array to be checked
     * @return True if the TeleportRequestHeader with the given player array is in
     *         the recent teleport queue, false otherwise
     */
    public boolean isTeleportQueued(TeleportRequestHeader requestHeader, Player[] teleportArray) {
        return teleportQueue.getIfPresent(requestHeader) != null
                ? Arrays.equals(teleportArray, teleportQueue.getIfPresent(requestHeader))
                : false;
    }

    /**
     * Checks if a player is in the recent teleport queue
     * 
     * @param player The player to be checked
     * @return The last player the input player was teleported to, null if the input
     *         player is not in the recent teleport queue
     */
    public Player getRecentTeleport(Player player) {
        return recentTeleportQueue.getIfPresent(player);
    }

    /**
     * Gets the nth player in the recent teleport queue for the given
     * TeleportRequestHeader
     * 
     * @param requestHeader The TeleportRequestHeader to be checked
     * @param i             The position in the player array to be retrieved
     * @return The nth player in the recent teleport queue for the given
     *         TeleportRequestHeader, null if the input player is not in the recent
     *         teleport queue
     */
    public Player getPlayer(TeleportRequestHeader requestHeader, int n) {
        return teleportQueue.getIfPresent(requestHeader) == null ? null
                : (teleportQueue.getIfPresent(requestHeader)[n].isActive()
                        ? teleportQueue.getIfPresent(requestHeader)[n]
                        : null);
    }

}
