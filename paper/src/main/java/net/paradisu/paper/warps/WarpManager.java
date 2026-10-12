/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.paradisu.paper.warps;

import de.themoep.connectorplugin.LocationInfo;
import de.themoep.connectorplugin.connector.MessageTarget;
import jakarta.persistence.EntityManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.core.utils.ExponentialBackoffLimiter;
import net.paradisu.core.utils.FuzzyMatcher;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.config.configs.MessagesConfig.Commands.Warp;
import net.paradisu.paper.config.configs.ParadisuConfig.Warps.RateLimit;
import net.paradisu.paper.messaging.ClusterPlayer;
import net.paradisu.paper.messaging.messages.SyncWarpsMessage;
import net.paradisu.paper.sync.WarpSync;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Caches warps from the database and teleports players to them, across servers when the warp belongs to another warp
 * context. Warps are reloaded whenever any server changes them (see {@link SyncWarpsMessage}).
 */
public class WarpManager {
    /** Seconds to wait for a cross-server teleport to be confirmed. */
    private static final long REMOTE_TELEPORT_TIMEOUT_SECONDS = 30L;

    private final ParadisuPaper paradisu;

    /** All warps, replaced atomically on sync; read from command parsers, which may run off the main thread. */
    private volatile List<WarpModel> warps = List.of();
    /** Warps by lower-case name. */
    private volatile Map<String, List<WarpModel>> warpsByName = Map.of();
    /** Warps by lower-case alias. */
    private volatile Map<String, List<WarpModel>> warpsByAlias = Map.of();
    /** Fuzzy index over names and aliases, rebuilt on sync. */
    private volatile FuzzyMatcher<WarpModel> matcher = FuzzyMatcher.empty();
    /** Rate limiter for /warp, keyed by player UUID. */
    private volatile ExponentialBackoffLimiter<UUID> rateLimiter;

    public WarpManager(ParadisuPaper paradisu) {
        this.paradisu = paradisu;
        this.reloadRateLimiter();
    }

    /** Rebuilds the /warp rate limiter from {@code paradisu.yml}. Clears all tracked players. */
    public void reloadRateLimiter() {
        RateLimit config = this.paradisu.paradisuConfig().warps().rateLimit();
        ExponentialBackoffLimiter.Settings settings;
        try {
            settings = new ExponentialBackoffLimiter.Settings(
                    config.burst(),
                    Duration.ofSeconds(config.refillSeconds()),
                    Duration.ofSeconds(config.baseTimeoutSeconds()),
                    Duration.ofSeconds(config.maxTimeoutSeconds()),
                    Duration.ofSeconds(config.strikeDecaySeconds()));
        } catch (IllegalArgumentException e) {
            this.paradisu.logger().warn("Invalid warps.rate-limit settings, using defaults: " + e.getMessage());
            RateLimit defaults = new RateLimit();
            settings = new ExponentialBackoffLimiter.Settings(
                    defaults.burst(),
                    Duration.ofSeconds(defaults.refillSeconds()),
                    Duration.ofSeconds(defaults.baseTimeoutSeconds()),
                    Duration.ofSeconds(defaults.maxTimeoutSeconds()),
                    Duration.ofSeconds(defaults.strikeDecaySeconds()));
        }
        this.rateLimiter = new ExponentialBackoffLimiter<>(settings);
    }

    /**
     * Returns the rate limiter for /warp, keyed by player UUID.
     *
     * @return the rate limiter
     */
    public ExponentialBackoffLimiter<UUID> rateLimiter() {
        return this.rateLimiter;
    }

    /** Reloads all warps from the database asynchronously. */
    public void syncWarps() {
        Bukkit.getScheduler().runTaskAsynchronously(this.paradisu, new WarpSync(this.paradisu));
    }

    /**
     * Replaces the cached warps. Called on the main thread by {@link WarpSync}.
     *
     * @param warps the detached warps loaded from the database
     */
    public void setWarps(List<WarpModel> warps) {
        Map<String, List<WarpModel>> byName = new HashMap<>();
        Map<String, List<WarpModel>> byAlias = new HashMap<>();
        FuzzyMatcher.Builder<WarpModel> matcherBuilder = FuzzyMatcher.builder();
        for (WarpModel warp : warps) {
            byName.computeIfAbsent(key(warp.name()), k -> new ArrayList<>()).add(warp);
            matcherBuilder.add(warp.name(), warp);
            for (String alias : aliases(warp)) {
                byAlias.computeIfAbsent(key(alias), k -> new ArrayList<>()).add(warp);
                matcherBuilder.add(alias, warp, true);
            }
        }
        this.matcher = matcherBuilder.build();
        this.warpsByName = Map.copyOf(byName);
        this.warpsByAlias = Map.copyOf(byAlias);
        this.warps = List.copyOf(warps);
    }

    /**
     * Returns all cached warps.
     *
     * @return the warps
     */
    public List<WarpModel> warps() {
        return this.warps;
    }

    /**
     * Resolves a warp by name or alias, case-insensitively. Names win over aliases, and warps in this server's warp
     * context win over warps in other contexts.
     *
     * @param input the warp name or alias
     * @return the warp, or null if none matches
     */
    public WarpModel closestWarp(String input) {
        String context = this.localContext();
        for (Map<String, List<WarpModel>> index : List.of(this.warpsByName, this.warpsByAlias)) {
            List<WarpModel> matches = index.get(key(input));
            if (matches != null && !matches.isEmpty()) {
                return matches.stream()
                        .filter(warp -> warp.context().equals(context))
                        .findFirst()
                        .orElse(matches.getFirst());
            }
        }
        return null;
    }

    /**
     * Resolves user input to a warp: an exact name or alias first (see {@link #closestWarp}), then the most similar
     * name or alias among the warps the sender may use (see {@link FuzzyMatcher}). Among equally good matches, warps in
     * this server's warp context and then shorter names win.
     *
     * @param input the user input
     * @param sender the sender, whose permissions limit the fuzzy candidates
     * @return the warp, or null if nothing is similar enough
     */
    public WarpModel resolveWarp(String input, CommandSender sender) {
        String trimmed = input.trim();
        WarpModel exact = this.closestWarp(trimmed);
        if (exact != null) {
            return exact;
        }

        String context = this.localContext();
        Comparator<WarpModel> preference = Comparator.<WarpModel>comparingInt(
                        warp -> warp.context().equals(context) ? 0 : 1)
                .thenComparingInt(warp -> warp.name().length())
                .thenComparing(WarpModel::name, String.CASE_INSENSITIVE_ORDER);
        return this.matcher
                .match(trimmed, warp -> this.canUse(sender, warp), preference)
                .orElse(null);
    }

    /**
     * Finds a warp by exact name (case-insensitive) in a warp context.
     *
     * @param name the warp name
     * @param context the warp context
     * @return the warp, or null if none exists
     */
    public WarpModel exactWarp(String name, String context) {
        return this.warpsByName.getOrDefault(key(name), List.of()).stream()
                .filter(warp -> warp.context().equals(context))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the distinct names of the warps a sender may use, for suggestions and listing.
     *
     * @param sender the sender, or null to include every warp
     * @return the warp names in alphabetical order
     */
    public List<String> warpNames(CommandSender sender) {
        return this.warps.stream()
                .filter(warp -> sender == null || this.canUse(sender, warp))
                .map(WarpModel::name)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * Returns the names of the warps in this server's warp context, for the management subcommands.
     *
     * @return the warp names in alphabetical order
     */
    public List<String> localWarpNames() {
        String context = this.localContext();
        return this.warps.stream()
                .filter(warp -> warp.context().equals(context))
                .map(WarpModel::name)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * Returns whether a sender may use a warp.
     *
     * @param sender the sender
     * @param warp the warp
     * @return true if the warp has no permission or the sender has it
     */
    public boolean canUse(CommandSender sender, WarpModel warp) {
        return warp.permission() == null || warp.permission().isBlank() || sender.hasPermission(warp.permission());
    }

    /**
     * Creates a warp. The returned future completes on the main thread.
     *
     * @param warp the new warp
     * @return a future that completes with whether the warp was saved
     */
    public CompletableFuture<Boolean> createWarp(WarpModel warp) {
        return this.write("Failed to create warp " + warp.name(), entityManager -> {
            entityManager.persist(warp);
            return true;
        });
    }

    /**
     * Changes a warp. The returned future completes on the main thread.
     *
     * @param id the id of the warp
     * @param changes the changes to apply to the managed warp
     * @return a future that completes with whether the warp existed and was saved
     */
    public CompletableFuture<Boolean> updateWarp(UUID id, Consumer<WarpModel> changes) {
        return this.write("Failed to update warp " + id, entityManager -> {
            WarpModel warp = entityManager.find(WarpModel.class, id);
            if (warp == null) {
                return false;
            }
            changes.accept(warp);
            return true;
        });
    }

    /**
     * Deletes a warp. The returned future completes on the main thread.
     *
     * @param id the id of the warp
     * @return a future that completes with whether the warp existed and was deleted
     */
    public CompletableFuture<Boolean> deleteWarp(UUID id) {
        return this.write("Failed to delete warp " + id, entityManager -> {
            WarpModel warp = entityManager.find(WarpModel.class, id);
            if (warp == null) {
                return false;
            }
            entityManager.remove(warp);
            return true;
        });
    }

    /**
     * Runs a write transaction asynchronously. On success every server, including this one, reloads its warps.
     *
     * @param failureMessage the message logged if the transaction fails
     * @param work the work to run inside the transaction; returns false to roll back
     * @return a future that completes on the main thread with whether the transaction was committed
     */
    private CompletableFuture<Boolean> write(String failureMessage, Function<EntityManager, Boolean> work) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(this.paradisu, () -> {
            boolean committed = false;
            try (EntityManager entityManager =
                    this.paradisu.databaseSession().factory().createEntityManager(); ) {
                try {
                    entityManager.getTransaction().begin();
                    if (work.apply(entityManager)) {
                        entityManager.getTransaction().commit();
                        committed = true;
                    } else {
                        entityManager.getTransaction().rollback();
                    }
                } catch (Exception e) {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                    throw e;
                }
            } catch (Exception e) {
                this.paradisu.logger().error(failureMessage, e);
            }

            final boolean result = committed;
            Bukkit.getScheduler().runTask(this.paradisu, () -> {
                if (result) {
                    this.paradisu.messagingManager().send(MessageTarget.OTHERS_QUEUE, new SyncWarpsMessage.Payload());
                    this.syncWarps();
                }
                future.complete(result);
            });
        });
        return future;
    }

    /**
     * Teleports a player anywhere in the cluster to a warp, moving them to a server that serves the warp's context if
     * needed. Must be called on the main thread.
     *
     * @param target the player to teleport
     * @param warp the warp
     * @param sender who requested the teleport; receives the feedback and must be allowed to use the warp
     */
    @SuppressWarnings("unchecked")
    public void teleport(ClusterPlayer target, WarpModel warp, CommandSender sender) {
        Warp text = this.paradisu.messagesConfig().commands().warp();
        if (!this.canUse(sender, warp)) {
            Messages.sendPrefixed(sender, text.output().get(0));
            return;
        }

        Optional<String> server =
                this.paradisu.messagingManager().findServerForContext(warp.context(), target.serverName());
        if (server.isEmpty()) {
            Messages.sendPrefixed(sender, text.output().get(3));
            return;
        }

        boolean self = sender instanceof Player p && p.getUniqueId().equals(target.uuid());
        if (self) {
            Messages.sendPrefixed(sender, text.output().get(1), Placeholder.unparsed("warp", warp.name()));
        } else {
            Messages.sendPrefixed(
                    sender,
                    text.output().get(2),
                    Placeholder.unparsed("player", target.name()),
                    Placeholder.unparsed("warp", warp.name()));
        }

        Player localPlayer = target.isLocal() ? Bukkit.getPlayer(target.uuid()) : null;
        String localServer = this.paradisu.connector().getServerName();
        if (localPlayer != null && server.get().equals(localServer)) {
            this.teleportLocal(localPlayer, warp, sender);
            return;
        }

        LocationInfo location =
                new LocationInfo(server.get(), warp.world(), warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch());
        this.paradisu
                .connector()
                .getBridge()
                .teleport(target.name(), location, result -> this.paradisu
                        .logger()
                        .info("Warp teleport of " + target.name() + " to " + warp.name() + ": " + result))
                .orTimeout(REMOTE_TELEPORT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .whenComplete((success, error) -> {
                    if (error == null && Boolean.TRUE.equals(success)) {
                        return;
                    }
                    if (error != null) {
                        this.paradisu.logger().warn("Warp teleport of " + target.name() + " failed", error);
                    }
                    Bukkit.getScheduler().runTask(this.paradisu, () -> this.sendFailure(sender, warp));
                });
    }

    /**
     * Teleports a player on this server to a warp in this server's warp context.
     *
     * @param player the player
     * @param warp the warp
     * @param sender who receives failure feedback
     */
    private void teleportLocal(Player player, WarpModel warp, CommandSender sender) {
        World world = Bukkit.getWorld(warp.world());
        if (world == null) {
            this.paradisu.logger().warn("World " + warp.world() + " of warp " + warp.name() + " does not exist");
            this.sendFailure(sender, warp);
            return;
        }

        Location location = new Location(world, warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch());
        player.teleportAsync(location).whenComplete((success, error) -> {
            if (error != null || !Boolean.TRUE.equals(success)) {
                Bukkit.getScheduler().runTask(this.paradisu, () -> this.sendFailure(sender, warp));
            }
        });
    }

    private void sendFailure(CommandSender sender, WarpModel warp) {
        Messages.sendPrefixed(
                sender,
                this.paradisu.messagesConfig().commands().warp().output().get(4),
                Placeholder.unparsed("warp", warp.name()));
    }

    /**
     * Returns the warp context of this server.
     *
     * @return the warp context
     */
    public String localContext() {
        return this.paradisu.paradisuConfig().context().warp();
    }

    /**
     * Returns a warp's aliases without blanks or duplicates.
     *
     * @param warp the warp
     * @return the aliases
     */
    public static Set<String> aliases(WarpModel warp) {
        Set<String> aliases = new LinkedHashSet<>();
        if (warp.aliases() != null) {
            Arrays.stream(warp.aliases())
                    .filter(alias -> alias != null && !alias.isBlank())
                    .forEach(aliases::add);
        }
        return aliases;
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
