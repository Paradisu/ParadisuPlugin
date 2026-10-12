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
import de.themoep.connectorplugin.LocationInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;
import net.paradisu.velocity.config.configs.MessagesConfig.Commands;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;

public final class BackCommand extends AbstractVelocityCommand {
    public BackCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("back", "return")
                .permission("vparadisu.back")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().back().helpMsg()))
                .optional(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .back()
                                .helpArgs()
                                .get(0)))
                .handler(this::backCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /back command
     *
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void backCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player player = (Player) context.getOrDefault("player", context.sender());
        LocationInfo previousLocation = history.getTeleport(player);
        boolean validRequest = (previousLocation != null);
        Commands.Back bText = paradisu.messagesConfig().commands().back();
        TagResolver.Single playerPlaceholder = Placeholder.component("player", Component.text(player.getUsername()));

        if (validRequest) {
            paradisu.connector().getBridge().getLocation(player).whenComplete((location, locationException) -> {
                if (locationException == null) {
                    history.addTeleport(player, location);
                    paradisu.connector()
                            .getBridge()
                            .teleport(player.getUsername(), previousLocation, m -> {})
                            .whenComplete((success, teleportException) -> {
                                if (success) {
                                    Messages.sendPrefixed(
                                            context.sender(), bText.output().get(0), playerPlaceholder);
                                } else {
                                    paradisu.logger().error("Error teleporting: " + teleportException.getMessage());
                                }
                            });
                } else {
                    paradisu.logger().error("Error getting location: " + locationException.getMessage());
                }
            });
        } else {
            Messages.sendPrefixed(context.sender(), bText.output().get(1), playerPlaceholder);
        }
    }
}
