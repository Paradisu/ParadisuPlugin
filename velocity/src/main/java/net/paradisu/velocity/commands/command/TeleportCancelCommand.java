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
import net.paradisu.velocity.commands.util.teleport.TeleportQueue;
import net.paradisu.velocity.commands.util.teleport.TeleportRequestHeader;
import net.paradisu.velocity.config.configs.MessagesConfig;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;

import java.util.List;

public final class TeleportCancelCommand extends AbstractVelocityCommand {
    public TeleportCancelCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        MessagesConfig.Commands.Tpc tText = paradisu.messagesConfig().commands().tpc();
        var builder = this.commandManager
                .commandBuilder("tpc", "tpcancel")
                .permission("vparadisu.tprh")
                .commandDescription(Description.of(tText.helpMsg()))
                .required(
                        "target",
                        PlayerParser.playerParser(),
                        Description.of(tText.helpArgs().get(0)))
                .handler(this::teleportCancelCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tprh command
     *
     * @param context the data specified on registration of the command
     */
    private void teleportCancelCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.sender();

        TeleportQueue queue = new TeleportQueue();
        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(player, target);

        boolean existingRequest = queue.isTeleportQueued(requestHeader);
        List<String> tpcOutput = paradisu.messagesConfig().commands().tpc().output();

        if (existingRequest) {
            queue.removeTeleport(requestHeader);
            Messages.sendPrefixedPlaceholder(target, tpcOutput.get(0), "player", player.getUsername());
            Messages.sendPrefixedPlaceholder(player, tpcOutput.get(1), "player", target.getUsername());
        } else {
            Messages.sendPrefixedPlaceholder(player, tpcOutput.get(2), "player", target.getUsername());
        }
    }
}
