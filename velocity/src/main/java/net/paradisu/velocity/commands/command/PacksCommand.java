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

package net.paradisu.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.velocity.parser.PlayerParser;

public class PacksCommand extends AbstractVelocityCommand {
    public PacksCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("packs")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().packs().helpMsg()));

        this.commandManager.command(builder.literal("clear")
                .permission("vparadisu.packs.clear")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().packs().clear().helpMsg()))
                .required(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .packs()
                                .clear()
                                .helpArgs()
                                .get(0)))
                .handler(this::clearCommand));

        this.commandManager.command(builder.literal("reload")
                .permission("vparadisu.packs.reload")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().packs().reload().helpMsg()))
                .required(
                        "resend",
                        BooleanParser.booleanParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .packs()
                                .reload()
                                .helpArgs()
                                .get(0)))
                .handler(this::reloadCommand));

        this.commandManager.command(builder.literal(("resend"))
                .permission("vparadisu.packs.resend")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().packs().resend().helpMsg()))
                .required(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .packs()
                                .resend()
                                .helpArgs()
                                .get(0)))
                .handler(this::resendCommand));
    }

    /**
     * Handler for the /packs clear command
     *
     * @param context the data specified on registration of the command
     */
    private void clearCommand(CommandContext<CommandSource> context) {
        Player player = context.getOrDefault("player", (Player) context.sender());

        player.clearResourcePacks();

        context.sender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(
                                paradisu.messagesConfig()
                                        .commands()
                                        .packs()
                                        .clear()
                                        .output()
                                        .get(0),
                                Placeholder.component("player", Component.text(player.getUsername())))));
    }

    /**
     * Handler for the /packs reload command
     *
     * @param context the data specified on registration of the command
     */
    private void reloadCommand(CommandContext<CommandSource> context) {
        paradisu.packManager().reload();

        boolean resend = context.getOrDefault("resend", false);

        if (resend) {
            paradisu.packManager().defaultRequest().whenCompleteAsync((packs, e) -> {
                if (e == null) {
                    paradisu.server().getAllPlayers().forEach(player -> {
                        player.sendResourcePacks(packs);
                    });
                }
            });
        }

        context.sender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .packs()
                                .reload()
                                .output()
                                .get(resend ? 1 : 0))));
    }

    /**
     * Handler for the /packs resend command
     *
     * @param context the data specified on registration of the command
     */
    private void resendCommand(CommandContext<CommandSource> context) {
        Player player = context.getOrDefault("player", (Player) context.sender());

        paradisu.packManager().defaultRequest().whenCompleteAsync((packs, e) -> {
            if (e == null) {
                player.sendResourcePacks(packs);
            }
        });

        context.sender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(
                                paradisu.messagesConfig()
                                        .commands()
                                        .packs()
                                        .resend()
                                        .output()
                                        .get(0),
                                Placeholder.component("player", Component.text(player.getUsername())))));
    }
}
