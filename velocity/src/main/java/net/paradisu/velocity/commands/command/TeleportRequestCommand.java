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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportQueue;
import net.paradisu.velocity.commands.util.teleport.TeleportRequestHeader;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;

public final class TeleportRequestCommand extends AbstractVelocityCommand {
    public TeleportRequestCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("tpr", "tprequest")
                .permission("vparadisu.tpr")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().tpr().helpMsg()))
                .required(
                        "target",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .tpr()
                                .helpArgs()
                                .get(0)))
                .handler(this::teleportRequestCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tpr command
     *
     * @param context the data specified on registration of the command
     */
    private void teleportRequestCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.sender();
        Player[] teleportArray = {player, target};

        TeleportQueue queue = new TeleportQueue();
        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(player, target);

        boolean existingRequest = queue.isTeleportQueued(requestHeader, teleportArray);

        if (!existingRequest) {
            queue.queueTeleport(requestHeader, teleportArray);

            String acceptCommand = "/tpa " + player.getUsername();
            target.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                    .deserialize(
                            paradisu.messagesConfig().commands().tpr().output().get(0),
                            Placeholder.component("player", Component.text(player.getUsername())),
                            Placeholder.component(
                                    "command",
                                    Component.text(acceptCommand)
                                            .clickEvent(ClickEvent.runCommand(acceptCommand))
                                            .hoverEvent(Component.text(acceptCommand)
                                                    .color(NamedTextColor.GREEN))))));

            player.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                    .deserialize(
                            paradisu.messagesConfig().commands().tpr().output().get(1),
                            Placeholder.component("player", Component.text(target.getUsername())))));
        } else {
            player.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                    .deserialize(
                            paradisu.messagesConfig().commands().tpr().output().get(2),
                            Placeholder.component("player", Component.text(target.getUsername())))));
        }
    }
}
