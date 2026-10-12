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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.core.utils.ExponentialBackoffLimiter;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import net.paradisu.paper.config.configs.MessagesConfig.Commands.Warp;
import net.paradisu.paper.config.configs.MessagesConfig.Commands.Warps;
import net.paradisu.paper.messaging.ClusterPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;

public class WarpCommand extends AbstractPaperCommand {
    /** Permission that exempts a player from the /warp rate limit. */
    public static final String BYPASS_RATE_LIMIT_PERMISSION = "paradisu.warp.bypass-ratelimit";

    public WarpCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        Warp wText = paradisu.messagesConfig().commands().warp();
        var warpBuilder = this.commandManager
                .commandBuilder("warp")
                .permission("paradisu.warp")
                .commandDescription(Description.of(wText.helpMsg()))
                .required(
                        "warp",
                        StringParser.greedyStringParser(),
                        Description.of(wText.helpArgs().get(0)),
                        warpSuggestions(this.paradisu))
                .handler(this::warpCommand);
        this.commandManager.command(warpBuilder);

        var warpsBuilder = this.commandManager
                .commandBuilder("warps")
                .permission("paradisu.warp")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().warps().helpMsg()))
                .handler(this::warpsCommand);
        this.commandManager.command(warpsBuilder);
    }

    /**
     * Suggests the names of the warps the sender may use.
     *
     * @param paradisu the plugin instance
     * @return the suggestion provider
     */
    public static SuggestionProvider<CommandSourceStack> warpSuggestions(ParadisuPaper paradisu) {
        return SuggestionProvider.blockingStrings((context, input) ->
                paradisu.warpManager().warpNames(context.sender().getSender()));
    }

    /**
     * Handler for the /warp command. Teleports the sender to the warp whose name is closest to the input. The rate
     * limit is checked before the name is resolved, so spam cannot trigger repeated fuzzy matching.
     *
     * @param context the data specified on registration of the command
     */
    private void warpCommand(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.sender().getSender();
        Warp wText = paradisu.messagesConfig().commands().warp();
        if (!(sender instanceof Player player)) {
            Messages.sendPrefixed(sender, wText.output().get(5));
            return;
        }

        if (!player.hasPermission(BYPASS_RATE_LIMIT_PERMISSION)) {
            ExponentialBackoffLimiter.Result limit =
                    paradisu.warpManager().rateLimiter().tryAcquire(player.getUniqueId());
            if (!limit.allowed()) {
                Messages.sendPrefixed(
                        player,
                        wText.output().get(7),
                        Placeholder.unparsed("seconds", String.valueOf(limit.retryAfterSeconds())));
                return;
            }
        }

        String input = context.get("warp");
        WarpModel warp = paradisu.warpManager().resolveWarp(input, player);
        if (warp == null) {
            Messages.sendPrefixed(player, wText.output().get(6), Placeholder.unparsed("warp", input));
            return;
        }

        paradisu.warpManager()
                .teleport(
                        new ClusterPlayer(
                                player.getUniqueId(),
                                player.getName(),
                                paradisu.connector().getServerName(),
                                true),
                        warp,
                        player);
    }

    /**
     * Handler for the /warps command. Lists the warps the sender may use; each name can be clicked to warp.
     *
     * @param context the data specified on registration of the command
     */
    private void warpsCommand(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.sender().getSender();
        List<String> names = paradisu.warpManager().warpNames(sender);
        Warps wText = paradisu.messagesConfig().commands().warps();

        if (names.isEmpty()) {
            Messages.sendPrefixed(sender, wText.output().get(1));
            return;
        }

        Component list = Component.join(
                JoinConfiguration.commas(true),
                names.stream()
                        .map(name -> Component.text(name).clickEvent(ClickEvent.runCommand("/warp " + name)))
                        .toList());
        Messages.sendPrefixed(sender, wText.output().get(0), Placeholder.component("warps", list));
    }
}
