/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
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

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.messaging.ClusterPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/** Parses a player online anywhere in the cluster, using the server heartbeats. */
@RequiredArgsConstructor
public class ClusterPlayerParser
        implements ArgumentParser<CommandSourceStack, ClusterPlayer>,
                BlockingSuggestionProvider.Strings<CommandSourceStack> {
    private final ParadisuPaper paradisu;

    /**
     * Creates a parser descriptor for cluster players.
     *
     * @param paradisu the plugin instance
     * @return the parser descriptor
     */
    public static ParserDescriptor<CommandSourceStack, ClusterPlayer> clusterPlayerParser(ParadisuPaper paradisu) {
        return ParserDescriptor.of(new ClusterPlayerParser(paradisu), ClusterPlayer.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull ClusterPlayer> parse(
            @NonNull CommandContext<@NonNull CommandSourceStack> commandContext, @NonNull CommandInput commandInput) {
        final String input = commandInput.readString();
        return this.paradisu
                .messagingManager()
                .findPlayer(input)
                .map(ArgumentParseResult::success)
                .orElseGet(() -> ArgumentParseResult.failure(
                        new IllegalArgumentException("Player not found in cluster: " + input)));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull CommandContext<CommandSourceStack> context, @NonNull CommandInput input) {
        return this.paradisu.messagingManager().onlinePlayerNames();
    }
}
