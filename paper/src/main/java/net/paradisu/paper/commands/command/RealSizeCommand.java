/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
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
import net.paradisu.core.locale.Messages;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;

public class RealSizeCommand extends AbstractPaperCommand {

    public RealSizeCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("realsize")
                .permission("paradisu.realsize")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().realSize().helpMsg()));

        this.commandManager.command(builder.literal("set")
                .required(
                        "feet",
                        IntegerParser.integerParser(0, 100),
                        SuggestionProvider.blockingStrings((context, input) -> List.of("5", "6")))
                .optional("inches", IntegerParser.integerParser(0, 11))
                .handler(this::realSize));

        this.commandManager.command(builder.literal("reset")
                .optional("player", PlayerParser.playerParser())
                .handler(context -> {
                    Player player = context.getOrDefault("player", null);
                    if (player == null && context.sender().getSender() instanceof Player sender) {
                        player = sender;
                    }
                    if (player == null) {
                        Messages.sendPrefixed(
                                context.sender().getSender(),
                                paradisu.messagesConfig().commands().realSize().playerRequired());
                        return;
                    }
                    player.getAttribute(Attribute.SCALE).setBaseValue(1.0);
                    Messages.sendPrefixedPlaceholder(
                            context.sender().getSender(),
                            paradisu.messagesConfig().commands().realSize().reset(),
                            "player",
                            player.getName());
                }));
    }

    /** Applies the requested height in feet and inches to the player. */
    public void realSize(CommandContext<CommandSourceStack> context) {
        if (!(context.sender().getSender() instanceof Player player)) {
            Messages.sendPrefixed(
                    context.sender().getSender(),
                    paradisu.messagesConfig().commands().realSize().playerRequired());
            return;
        }

        int feet = context.get("feet");
        int inches = context.getOrDefault("inches", 0);
        int totalinches = feet * 12 + inches;

        if (totalinches <= 76 && totalinches >= 60) { // this is in inches
            player.getAttribute(Attribute.SCALE).setBaseValue(calculatePlayerSize(totalinches));
            player.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                    .deserialize(paradisu.messagesConfig()
                            .commands()
                            .realSize()
                            .output()
                            .get(0))));

        } else if (totalinches >= 76) {
            player.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                    .deserialize(paradisu.messagesConfig().commands().realSize().tootall())));
        } else {
            player.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                    .deserialize(paradisu.messagesConfig().commands().realSize().tooshort())));
        }
    }

    /** Converts a height in inches to the player scale relative to 1.85 meters. */
    public double calculatePlayerSize(int totalinches) {
        Double totalheight = totalinches * 0.0254;
        totalheight = totalheight / 1.85;
        return totalheight;
    }
}
