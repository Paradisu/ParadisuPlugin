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

package net.paradisu.paper.commands.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.paradisu.core.locale.Messages;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import net.paradisu.paper.inventory.ModelGiveInventory;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

public class ModelGiveCommand extends AbstractPaperCommand {

    public ModelGiveCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("mgive")
                .permission("paradisu.mgive")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().mgive().helpMsg()));

        this.commandManager.command(builder.literal("search")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().mgive().searchHelp()))
                .required(
                        "query",
                        StringParser.greedyStringParser(),
                        Description.of(
                                paradisu.messagesConfig().commands().mgive().searchArgument()))
                .handler(this::mgiveCommand));

        builder = builder.optional(
                        "tag",
                        StringParser.greedyStringParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .mgive()
                                .helpArgs()
                                .get(0)),
                        SuggestionProvider.blockingStrings(
                                (context, input) -> paradisu.modelManager().tags()))
                .handler(this::mgiveCommand);

        this.commandManager.command(builder);
    }

    private void mgiveCommand(CommandContext<CommandSourceStack> context) {

        if (!(context.sender().getSender() instanceof Player player)) {
            Messages.sendPrefixed(
                    context.sender().getSender(),
                    paradisu.messagesConfig().commands().mgive().output().get(3));
            return;
        }

        ModelGiveInventory.openPage(
                paradisu, player, 0, context.getOrDefault("tag", ""), context.getOrDefault("query", ""), true);
    }
}
