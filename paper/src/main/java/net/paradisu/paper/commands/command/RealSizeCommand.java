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
import net.paradisu.core.locale.Messages;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

public class RealSizeCommand extends AbstractPaperCommand {

    public RealSizeCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("realsize")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().realSize().helpMsg()));

        this.commandManager.command(builder.literal("set")
                .required("feet", PlayerParser.playerParser())
                .permission("paradisu.realsize")
                .optional("inches", PlayerParser.playerParser())
                .handler(this::realSize));

        this.commandManager.command(
                builder.literal("reset").handler(context -> {
                    Player player = (Player) context.sender();
                    player.getAttribute(Attribute.SCALE).setBaseValue(1.0);
                    // this.helpManager.getMinecraftHelp().queryCommands(player.getAttribute(Attribute.SCALE).setBaseValue(1.0), context.sender());
                }));

        this.commandManager.command(builder);
    }

    public void realSize(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.sender().getSender();

        int feet = Integer.parseInt(context.get("feet"));
        int inches = Integer.parseInt(context.getOrDefault("inches", "0"));
        int totalinches = feet + inches; // totalinches

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

    public double calculatePlayerSize(int totalinches) {
        Double totalheight = totalinches * 0.0254;
        totalheight = totalheight / 1.85;
        return totalheight;
    }
}
