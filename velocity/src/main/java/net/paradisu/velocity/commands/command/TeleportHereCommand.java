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
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;
import net.paradisu.velocity.config.configs.MessagesConfig;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;

public final class TeleportHereCommand extends AbstractVelocityCommand {
    public TeleportHereCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        MessagesConfig.Commands.Tph tText = paradisu.messagesConfig().commands().tph();
        var builder = this.commandManager
                .commandBuilder("tph", "tphere")
                .permission("vparadisu.tph")
                .commandDescription(Description.of(tText.helpMsg()))
                .required(
                        "target",
                        PlayerParser.playerParser(),
                        Description.of(tText.helpArgs().get(0)))
                .handler(this::teleportCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tph command
     *
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player target = (Player) context.get("target");
        Player player = (Player) context.sender();

        paradisu.connector().getBridge().getLocation(target).whenComplete((location, locationException) -> {
            if (locationException == null) {
                history.addTeleport(target, location);
                paradisu.connector()
                        .getBridge()
                        .teleport(target.getUsername(), player.getUsername(), m -> {})
                        .whenComplete((success, teleportException) -> {
                            if (success) {
                                Messages.sendPrefixedPlaceholder(
                                        player,
                                        paradisu.messagesConfig()
                                                .commands()
                                                .tph()
                                                .output()
                                                .get(0),
                                        "player",
                                        target.getUsername());
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
