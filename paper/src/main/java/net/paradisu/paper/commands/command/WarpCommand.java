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

package net.paradisu.paper.commands.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import net.paradisu.paper.commands.parser.ClusterPlayerParser;
import net.paradisu.paper.commands.parser.WarpParser;
import net.paradisu.paper.config.configs.MessagesConfig.Commands.Warp;
import net.paradisu.paper.messaging.ClusterPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;

import java.util.Optional;

public class WarpCommand extends AbstractPaperCommand {
    public WarpCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        Warp wText = paradisu.messagesConfig().commands().warp();
        this.commandManager.command(this.commandManager
                .commandBuilder("warp")
                .permission("paradisu.command.warp")
                .commandDescription(CommandDescription.commandDescription(wText.helpMsg()))
                .argument(CommandComponent.<CommandSourceStack, WarpModel>builder()
                        .name("warp")
                        .valueType(WarpModel.class)
                        .parser(new WarpParser<>(this.paradisu))
                        .description(Description.of(wText.helpArgs().get(0)))
                        .build())
                .optional(
                        "player",
                        CommandComponent.<CommandSourceStack, ClusterPlayer>builder()
                                .valueType(ClusterPlayer.class)
                                .parser(new ClusterPlayerParser<>(this.paradisu))
                                .description(Description.of(wText.helpArgs().get(1))))
                .handler(context -> {
                    final CommandSourceStack source = context.sender();
                    final WarpModel warp = context.get("warp");
                    final Optional<ClusterPlayer> targetOpt = context.optional("player");

                    if (targetOpt.isPresent()) {
                        ClusterPlayer target = targetOpt.get();
                        if (!source.getSender().hasPermission("paradisu.command.warp.other")) {
                            Messages.sendPrefixed(
                                    source.getSender(), wText.output().get(0));
                            return;
                        }

                        if (target.isLocal()) {
                            Player p = Bukkit.getPlayer(target.uuid());
                            if (p != null) {
                                paradisu.warpManager().teleportPlayer(p, warp, (Player)
                                        (source.getSender() instanceof Player s ? s : null));
                            }
                        } else {
                            paradisu.messagingManager().teleportRemotePlayer(target, warp);
                            Messages.sendPrefixed(
                                    source.getSender(),
                                    MiniMessage.miniMessage()
                                            .deserialize(
                                                    wText.output().get(2),
                                                    Placeholder.parsed("player", target.name()),
                                                    Placeholder.parsed("warp", warp.name())));
                        }
                    } else {
                        if (!(source.getSender() instanceof Player p)) return;
                        paradisu.warpManager().teleportPlayer(p, warp, p);
                    }
                }));
    }
}
