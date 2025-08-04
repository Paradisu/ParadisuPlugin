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
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.StringParser;

public final class ParadisuCommand extends AbstractPaperCommand {
    public ParadisuCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("paradisu")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().paradisu().helpMsg()));

        this.commandManager.command(builder.literal("help")
                .optional("query", StringParser.greedyStringParser())
                .handler(context -> {
                    this.helpManager
                            .getMinecraftHelp()
                            .queryCommands(context.getOrDefault("query", ""), context.sender());
                }));

        this.commandManager.command(builder.literal("about")
                .permission("paradisu.about")
                .commandDescription(CommandDescription.commandDescription(
                        paradisu.messagesConfig().commands().paradisu().about().helpMsg()))
                .handler(this::aboutCommand));

        this.commandManager.command(builder.literal("reload")
                .permission("paradisu.reload")
                .commandDescription(CommandDescription.commandDescription(
                        paradisu.messagesConfig().commands().paradisu().reload().helpMsg()))
                .handler(this::reloadCommand));

        var warpBuilder = builder.literal("warp");

        this.commandManager.command(warpBuilder
                .literal("create")
                .permission("paradisu.warp.create")
                .commandDescription(CommandDescription.commandDescription(paradisu.messagesConfig()
                        .commands()
                        .paradisu()
                        .warp()
                        .create()
                        .helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .create()
                                .helpArgs()
                                .get(0)))
                .optional(
                        "permission",
                        StringParser.stringParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .create()
                                .helpArgs()
                                .get(1)))
                .handler(this::createWarpCommand));

        this.commandManager.command(warpBuilder
                .literal("update")
                .permission("paradisu.warp.update")
                .commandDescription(CommandDescription.commandDescription(paradisu.messagesConfig()
                        .commands()
                        .paradisu()
                        .warp()
                        .update()
                        .helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .update()
                                .helpArgs()
                                .get(0)))
                .optional(
                        "permission",
                        StringParser.stringParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .update()
                                .helpArgs()
                                .get(1)))
                .handler(this::updateWarpCommand));

        this.commandManager.command(warpBuilder
                .literal("delete")
                .permission("paradisu.warp.delete")
                .commandDescription(CommandDescription.commandDescription(paradisu.messagesConfig()
                        .commands()
                        .paradisu()
                        .warp()
                        .delete()
                        .helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .delete()
                                .helpArgs()
                                .get(0)))
                .handler(this::deleteWarpCommand));
    }

    /**
     * Handeler for the /paradisu about command
     *
     * @param context the data specified on registration of the command
     */
    private void aboutCommand(CommandContext<CommandSourceStack> context) {
        context.sender()
                .getSender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .about()
                                .output()
                                .get(0))));
    }

    /**
     * Handeler for the /paradisu reload command
     *
     * @param context the data specified on registration of the command
     */
    private void reloadCommand(CommandContext<CommandSourceStack> context) {
        paradisu.reload();
        context.sender()
                .getSender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .reload()
                                .output()
                                .get(0))));
    }

    /**
     * Handler for the /paradisu warp create command
     *
     * @param context the data specified on registration of the command
     */
    private void createWarpCommand(CommandContext<CommandSourceStack> context) {
        String name = context.get("name");
        String permission = context.getOrDefault("permission", null);

        Player sender = (Player) context.sender().getSender();

        WarpModel warp = WarpModel.builder()
                .name(name)
                .permission(permission)
                .context(paradisu.paradisuConfig().context().warp())
                .x(sender.getLocation().getX())
                .y(sender.getLocation().getY())
                .z(sender.getLocation().getZ())
                .yaw(sender.getLocation().getYaw())
                .pitch(sender.getLocation().getPitch())
                .build();

        paradisu.warpManager().createWarp(warp).thenAccept(success -> {
            if (success) {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .create()
                                .output()
                                .get(0))));
            } else {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .create()
                                .output()
                                .get(1))));
            }
        });
    }

    /**
     * Handler for the /paradisu warp update command
     *
     * @param context the data specified on registration of the command
     */
    private void updateWarpCommand(CommandContext<CommandSourceStack> context) {
        String name = context.get("name");
        String permission = context.getOrDefault("permission", null);

        Player sender = (Player) context.sender().getSender();

        WarpModel warp = WarpModel.builder()
                .name(name)
                .permission(permission)
                .context(paradisu.paradisuConfig().context().warp())
                .x(sender.getLocation().getX())
                .y(sender.getLocation().getY())
                .z(sender.getLocation().getZ())
                .yaw(sender.getLocation().getYaw())
                .pitch(sender.getLocation().getPitch())
                .build();

        paradisu.warpManager().updateWarp(warp).thenAccept(success -> {
            if (success) {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .update()
                                .output()
                                .get(0))));
            } else {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .update()
                                .output()
                                .get(1))));
            }
        });
    }

    /**
     * Handler for the /paradisu warp delete command
     *
     * @param context the data specified on registration of the command
     */
    private void deleteWarpCommand(CommandContext<CommandSourceStack> context) {
        String name = context.get("name");
        Player sender = (Player) context.sender().getSender();

        paradisu.warpManager().deleteWarp(name).thenAccept(success -> {
            if (success) {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .delete()
                                .output()
                                .get(0))));
            } else {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .delete()
                                .output()
                                .get(1))));
            }
        });
    }
}
