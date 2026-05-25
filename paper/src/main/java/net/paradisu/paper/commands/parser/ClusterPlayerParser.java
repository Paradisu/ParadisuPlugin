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

package net.paradisu.paper.commands.parser;

import lombok.RequiredArgsConstructor;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.messaging.ClusterPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.Optional;

@RequiredArgsConstructor
public class ClusterPlayerParser<C> implements ArgumentParser<C, ClusterPlayer>, BlockingSuggestionProvider.Strings<C> {
    private final ParadisuPaper paradisu;

    @Override
    public @NonNull ArgumentParseResult<@NonNull ClusterPlayer> parse(
            @NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {

        final String input = commandInput.readString();

        Player localPlayer = Bukkit.getPlayer(input);
        if (localPlayer != null) {
            return ArgumentParseResult.success(new ClusterPlayer(
                    localPlayer.getUniqueId(),
                    localPlayer.getName(),
                    paradisu.connector().getServerName(),
                    true));
        }

        Optional<ClusterPlayer> remotePlayer = paradisu.messagingManager().clusterState().values().stream()
                .filter(state -> System.currentTimeMillis() - state.lastHeartbeat() < 15000)
                .flatMap(state -> state.onlinePlayers().stream()
                        .filter(p -> p.name().equalsIgnoreCase(input))
                        .map(p -> new ClusterPlayer(p.uuid(), p.name(), state.serverName(), false)))
                .findFirst();

        return remotePlayer
                .map(ArgumentParseResult::success)
                .orElseGet(() -> ArgumentParseResult.failure(
                        new IllegalArgumentException("Player not found in cluster: " + input)));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> context, @NonNull CommandInput input) {
        return paradisu.messagingManager().getAllOnlinePlayerNames();
    }
}
