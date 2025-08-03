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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.themoep.connectorplugin.LocationInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.DoubleParser;
import org.incendo.cloud.velocity.parser.PlayerParser;
import org.incendo.cloud.velocity.parser.ServerParser;

public final class TeleportPositionCommand extends AbstractVelocityCommand {
    public TeleportPositionCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("tppos", "tpposition")
                .permission("vparadisu.tppos")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().tppos().helpMsg()))
                .required(
                        "x",
                        DoubleParser.doubleParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .tppos()
                                .helpArgs()
                                .get(0)))
                .required(
                        "y",
                        DoubleParser.doubleParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .tppos()
                                .helpArgs()
                                .get(1)))
                .required(
                        "z",
                        DoubleParser.doubleParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .tppos()
                                .helpArgs()
                                .get(2)))
                .optional(
                        "server",
                        ServerParser.serverParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .tppos()
                                .helpArgs()
                                .get(3)))
                .optional(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .tppos()
                                .helpArgs()
                                .get(4)))
                .handler(this::teleportPositionCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tppos command
     *
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportPositionCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player player = (Player) context.getOrDefault("player", context.sender());

        paradisu.connector().getBridge().getLocation(player).whenComplete((location, locationException) -> {
            if (locationException == null) {
                history.addTeleport(player, location);
                RegisteredServer server = context.getOrDefault("server", null);
                LocationInfo telportLocation = new LocationInfo(
                        server == null
                                ? location.getServer()
                                : server.getServerInfo().getName(),
                        location.getWorld(),
                        context.get("x"),
                        context.get("y"),
                        context.get("z"));
                paradisu.connector()
                        .getBridge()
                        .teleport(player.getUsername(), telportLocation, m -> {})
                        .whenComplete((success, teleportException) -> {
                            if (success) {
                                context.sender()
                                        .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                                                .deserialize(
                                                        paradisu.messagesConfig()
                                                                .commands()
                                                                .tppos()
                                                                .output()
                                                                .get(0),
                                                        Placeholder.component(
                                                                "player", Component.text(player.getUsername())),
                                                        Placeholder.component(
                                                                "posx", Component.text((int) telportLocation.getX())),
                                                        Placeholder.component(
                                                                "posy", Component.text((int) telportLocation.getY())),
                                                        Placeholder.component(
                                                                "posz", Component.text((int) telportLocation.getZ())),
                                                        Placeholder.component(
                                                                "server",
                                                                Component.text(telportLocation.getServer())))));
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
