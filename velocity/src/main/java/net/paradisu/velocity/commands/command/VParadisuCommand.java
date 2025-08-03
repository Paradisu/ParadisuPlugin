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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.StringParser;

import java.net.InetSocketAddress;

public final class VParadisuCommand extends AbstractVelocityCommand {
    public VParadisuCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("vparadisu")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().vparadisu().helpMsg()));

        this.commandManager.command(builder.literal("help")
                .optional("query", StringParser.greedyStringParser())
                .handler(context -> {
                    this.helpManager
                            .getMinecraftHelp()
                            .queryCommands(context.getOrDefault("query", ""), context.sender());
                }));

        this.commandManager.command(builder.literal("about")
                .permission("vparadisu.about")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().vparadisu().about().helpMsg()))
                .handler(this::aboutCommand));

        this.commandManager.command(builder.literal("maintenance")
                .permission("vparadisu.maintenance")
                .commandDescription(Description.of(paradisu.messagesConfig()
                        .commands()
                        .vparadisu()
                        .maintenance()
                        .helpMsg()))
                .handler(this::maintenanceCommand));

        this.commandManager.command(builder.literal("reload")
                .permission("vparadisu.reload")
                .commandDescription(Description.of(paradisu.messagesConfig()
                        .commands()
                        .vparadisu()
                        .reload()
                        .helpMsg()))
                .handler(this::reloadCommand));
    }

    /**
     * Handeler for the /vparadisu about command
     *
     * @param context the data specified on registration of the command
     */
    private void aboutCommand(CommandContext<CommandSource> context) {
        context.sender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .vparadisu()
                                .about()
                                .output()
                                .get(0))));
    }

    /**
     * Handeler for the /vparadisu maintenance command
     *
     * @param context the data specified on registration of the command
     */
    private void maintenanceCommand(CommandContext<CommandSource> context) {
        final InetSocketAddress limboAddress = new InetSocketAddress(
                paradisu.paradisuConfig().limboServer().host(),
                paradisu.paradisuConfig().limboServer().port());

        for (Player player : paradisu.server().getAllPlayers()) {
            player.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                    .deserialize(paradisu.messagesConfig()
                            .commands()
                            .vparadisu()
                            .maintenance()
                            .output()
                            .get(0))));
            player.transferToHost(limboAddress);
        }
    }

    /**
     * Handeler for the /vparadisu reload command
     *
     * @param context the data specified on registration of the command
     */
    private void reloadCommand(CommandContext<CommandSource> context) {
        paradisu.reload();
        context.sender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .vparadisu()
                                .reload()
                                .output()
                                .get(0))));
    }
}
