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
import org.incendo.cloud.velocity.parser.PlayerParser;

public final class LocateCommand extends AbstractVelocityCommand {
    public LocateCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("locate", "locateplayer", "find", "findplayer")
                .permission("vparadisu.locate")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().locate().helpMsg()))
                .required(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .locate()
                                .helpArgs()
                                .get(0)))
                .handler(this::locateCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /locate command
     *
     * @param context the data specified on registration of the command
     */
    private void locateCommand(CommandContext<CommandSource> context) {
        Player player = (Player) context.get("player");

        paradisu.connector().getBridge().getLocation(player).whenComplete((location, exception) -> {
            if (exception == null) {
                context.sender()
                        .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                                .deserialize(
                                        paradisu.messagesConfig()
                                                .commands()
                                                .locate()
                                                .output()
                                                .get(0),
                                        Placeholder.component("player", Component.text(player.getUsername())),
                                        Placeholder.component("server", Component.text(location.getServer())),
                                        Placeholder.component("posx", Component.text((int) location.getX())),
                                        Placeholder.component("posy", Component.text((int) location.getY())),
                                        Placeholder.component("posz", Component.text((int) location.getZ())))));
            } else {
                paradisu.logger().error("Error getting location: " + exception.getMessage());
            }
        });
    }
}
