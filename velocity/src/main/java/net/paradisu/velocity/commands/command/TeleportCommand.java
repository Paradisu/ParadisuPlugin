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
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;
import net.paradisu.velocity.config.configs.MessagesConfig;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;

public final class TeleportCommand extends AbstractVelocityCommand {
    public TeleportCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        MessagesConfig.Commands.Tp tText = paradisu.messagesConfig().commands().tp();
        var builder = this.commandManager
                .commandBuilder("tp", "vteleport")
                .permission("vparadisu.tp")
                .commandDescription(Description.of(tText.helpMsg()))
                .required(
                        "target",
                        PlayerParser.playerParser(),
                        Description.of(tText.helpArgs().get(0)))
                .optional(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(tText.helpArgs().get(1)))
                .handler(this::teleportHereCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tp command
     *
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportHereCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player target = (Player) context.get("target");
        Player player = (Player) context.getOrDefault("player", context.sender());

        paradisu.connector().getBridge().getLocation(player).whenComplete((location, locationException) -> {
            if (locationException == null) {
                history.addTeleport(player, location);
                paradisu.connector()
                        .getBridge()
                        .teleport(player.getUsername(), target.getUsername(), m -> {})
                        .whenComplete((success, teleportException) -> {
                            if (success) {
                                context.sender()
                                        .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                                                .deserialize(
                                                        paradisu.messagesConfig()
                                                                .commands()
                                                                .tp()
                                                                .output()
                                                                .get(0),
                                                        Placeholder.component(
                                                                "player", Component.text(player.getUsername())),
                                                        Placeholder.component(
                                                                "target", Component.text(target.getUsername())))));
                            } else {
                                paradisu.logger().error("Error teleporting: " + teleportException.getMessage());
                            }
                        });
            } else {
                paradisu.logger().error("Error getting location: " + locationException.getMessage());
            }
        });
    }
}
