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

import lombok.RequiredArgsConstructor;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@RequiredArgsConstructor
public class WarpParser<C> implements ArgumentParser<C, WarpModel>, BlockingSuggestionProvider.Strings<C> {
    private final ParadisuPaper paradisu;

    @Override
    public @NonNull ArgumentParseResult<@NonNull WarpModel> parse(
            @NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
        final String input = commandInput.readString();
        WarpModel warp = paradisu.warpManager().closestWarp(input);
        if (warp == null) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Warp not found: " + input));
        }
        return ArgumentParseResult.success(warp);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> context, @NonNull CommandInput input) {
        return paradisu.warpManager().warpNames();
    }
}
