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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.paradisu.core.locale.Messages;
import net.paradisu.core.utils.FuzzyMatcher;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import net.paradisu.paper.commands.parser.ClusterPlayerParser;
import net.paradisu.paper.config.configs.MessagesConfig.Commands.Paradisu;
import net.paradisu.paper.messaging.ClusterPlayer;
import net.paradisu.paper.warps.WarpManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class ParadisuCommand extends AbstractPaperCommand {
    /** Warp names: up to the fuzzy matcher's input limit, no quotes or backslashes (they break quoted arguments). */
    private static final Pattern VALID_NAME = Pattern.compile("[^\"\\\\]{1," + FuzzyMatcher.MAX_INPUT_LENGTH + "}");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public ParadisuCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("paradisu")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().paradisu().helpMsg()));
        Paradisu cText = paradisu.messagesConfig().commands().paradisu();

        this.commandManager.command(builder.literal("help")
                .optional("query", StringParser.greedyStringParser())
                .handler(context -> {
                    this.helpManager
                            .getMinecraftHelp()
                            .queryCommands(context.getOrDefault("query", ""), context.sender());
                }));

        this.commandManager.command(builder.literal("about")
                .permission("paradisu.about")
                .commandDescription(
                        CommandDescription.commandDescription(cText.about().helpMsg()))
                .handler(this::aboutCommand));

        this.commandManager.command(builder.literal("reload")
                .permission("paradisu.reload")
                .commandDescription(
                        CommandDescription.commandDescription(cText.reload().helpMsg()))
                .handler(this::reloadCommand));

        this.commandManager.command(builder.literal("items")
                .literal("reload")
                .permission("paradisu.items.reload")
                .commandDescription(Description.of(cText.items().reload().helpMsg()))
                .handler(this::reloadItemsCommand));

        var warpBuilder = builder.literal("warp");
        Paradisu.Warp wText = cText.warp();
        // Names are quoted strings, so suggest names containing spaces in quotes
        SuggestionProvider<CommandSourceStack> localWarps =
                SuggestionProvider.blockingStrings((context, input) -> paradisu.warpManager().localWarpNames().stream()
                        .map(name -> name.contains(" ") ? '"' + name + '"' : name)
                        .toList());

        this.commandManager.command(warpBuilder
                .literal("send")
                .permission("paradisu.warp.other")
                .commandDescription(
                        CommandDescription.commandDescription(wText.send().helpMsg()))
                .required(
                        "player",
                        ClusterPlayerParser.clusterPlayerParser(this.paradisu),
                        Description.of(wText.send().helpArgs().get(0)))
                .required(
                        "warp",
                        StringParser.greedyStringParser(),
                        Description.of(wText.send().helpArgs().get(1)),
                        WarpCommand.warpSuggestions(this.paradisu))
                .handler(this::sendWarpCommand));

        this.commandManager.command(warpBuilder
                .literal("create")
                .permission("paradisu.warp.create")
                .commandDescription(
                        CommandDescription.commandDescription(wText.create().helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(wText.create().helpArgs().get(0)))
                .optional(
                        "permission",
                        StringParser.stringParser(),
                        Description.of(wText.create().helpArgs().get(1)))
                .handler(this::createWarpCommand));

        this.commandManager.command(warpBuilder
                .literal("update")
                .permission("paradisu.warp.update")
                .commandDescription(
                        CommandDescription.commandDescription(wText.update().helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(wText.update().helpArgs().get(0)),
                        localWarps)
                .handler(this::updateWarpCommand));

        this.commandManager.command(warpBuilder
                .literal("delete")
                .permission("paradisu.warp.delete")
                .commandDescription(
                        CommandDescription.commandDescription(wText.delete().helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(wText.delete().helpArgs().get(0)),
                        localWarps)
                .handler(this::deleteWarpCommand));

        this.commandManager.command(warpBuilder
                .literal("permission")
                .permission("paradisu.warp.update")
                .commandDescription(
                        CommandDescription.commandDescription(wText.permission().helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(wText.permission().helpArgs().get(0)),
                        localWarps)
                .optional(
                        "permission",
                        StringParser.stringParser(),
                        Description.of(wText.permission().helpArgs().get(1)))
                .handler(this::permissionWarpCommand));

        this.commandManager.command(warpBuilder
                .literal("aliases")
                .permission("paradisu.warp.update")
                .commandDescription(
                        CommandDescription.commandDescription(wText.aliases().helpMsg()))
                .required(
                        "name",
                        StringParser.quotedStringParser(),
                        Description.of(wText.aliases().helpArgs().get(0)),
                        localWarps)
                .optional(
                        "aliases",
                        StringParser.greedyStringParser(),
                        Description.of(wText.aliases().helpArgs().get(1)))
                .handler(this::aliasesWarpCommand));
    }

    /**
     * Handeler for the /paradisu about command
     *
     * @param context the data specified on registration of the command
     */
    private void aboutCommand(CommandContext<CommandSourceStack> context) {
        Messages.sendPrefixed(
                context.sender().getSender(),
                paradisu.messagesConfig().commands().paradisu().about().output().get(0));
    }

    /**
     * Handeler for the /paradisu reload command
     *
     * @param context the data specified on registration of the command
     */
    private void reloadCommand(CommandContext<CommandSourceStack> context) {
        paradisu.reload();
        Messages.sendPrefixed(
                context.sender().getSender(),
                paradisu.messagesConfig()
                        .commands()
                        .paradisu()
                        .reload()
                        .output()
                        .get(0));
    }

    /** Refreshes the local item cache and reports the database result. */
    private void reloadItemsCommand(CommandContext<CommandSourceStack> context) {
        var sender = context.sender().getSender();
        paradisu.modelManager()
                .syncModels()
                .thenAccept(success -> Messages.sendPrefixed(
                        sender,
                        paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .items()
                                .reload()
                                .output()
                                .get(success ? 0 : 1)));
    }

    /**
     * Handler for the /paradisu warp send command. Teleports any player in the cluster to the warp whose name is
     * closest to the input. Not rate limited, since it requires {@code paradisu.warp.other}.
     *
     * @param context the data specified on registration of the command
     */
    private void sendWarpCommand(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.sender().getSender();
        ClusterPlayer target = context.get("player");
        String input = context.get("warp");
        WarpModel warp = paradisu.warpManager().resolveWarp(input, sender);
        if (warp == null) {
            Messages.sendPrefixed(
                    sender,
                    paradisu.messagesConfig().commands().warp().output().get(6),
                    Placeholder.unparsed("warp", input));
            return;
        }
        paradisu.warpManager().teleport(target, warp, sender);
    }

    /**
     * Handler for the /paradisu warp create command. Creates a warp at the sender's location in this server's warp
     * context.
     *
     * @param context the data specified on registration of the command
     */
    private void createWarpCommand(CommandContext<CommandSourceStack> context) {
        String name = WHITESPACE.matcher(context.<String>get("name").trim()).replaceAll(" ");
        Paradisu.Warp wText = paradisu.messagesConfig().commands().paradisu().warp();
        if (!(context.sender().getSender() instanceof Player sender)) {
            Messages.sendPrefixed(context.sender().getSender(), wText.output().get(1));
            return;
        }
        if (!VALID_NAME.matcher(name).matches()) {
            Messages.sendPrefixed(
                    sender,
                    wText.create().output().get(2),
                    Placeholder.unparsed("max", String.valueOf(FuzzyMatcher.MAX_INPUT_LENGTH)));
            return;
        }

        String warpContext = paradisu.warpManager().localContext();
        if (paradisu.warpManager().exactWarp(name, warpContext) != null) {
            Messages.sendPrefixed(sender, wText.create().output().get(1), Placeholder.unparsed("warp", name));
            return;
        }

        Location location = sender.getLocation();
        WarpModel warp = WarpModel.builder()
                .name(name)
                .permission(context.getOrDefault("permission", null))
                .context(warpContext)
                .aliases(new String[0])
                .world(location.getWorld().getName())
                .x(location.getX())
                .y(location.getY())
                .z(location.getZ())
                .yaw(location.getYaw())
                .pitch(location.getPitch())
                .build();

        this.reply(
                sender,
                name,
                paradisu.warpManager().createWarp(warp),
                wText.create().output().get(0));
    }

    /**
     * Handler for the /paradisu warp update command. Moves a warp in this server's warp context to the sender's
     * location.
     *
     * @param context the data specified on registration of the command
     */
    private void updateWarpCommand(CommandContext<CommandSourceStack> context) {
        Paradisu.Warp wText = paradisu.messagesConfig().commands().paradisu().warp();
        if (!(context.sender().getSender() instanceof Player sender)) {
            Messages.sendPrefixed(context.sender().getSender(), wText.output().get(1));
            return;
        }
        WarpModel warp = this.localWarp(sender, context.get("name"));
        if (warp == null) {
            return;
        }

        // Capture plain values on the main thread; the update runs asynchronously
        Location location = sender.getLocation();
        String world = location.getWorld().getName();
        double x = location.getX(), y = location.getY(), z = location.getZ();
        float yaw = location.getYaw(), pitch = location.getPitch();
        CompletableFuture<Boolean> result = paradisu.warpManager().updateWarp(warp.id(), managed -> {
            managed.world(world);
            managed.x(x);
            managed.y(y);
            managed.z(z);
            managed.yaw(yaw);
            managed.pitch(pitch);
        });
        this.reply(sender, warp.name(), result, wText.update().output().get(0));
    }

    /**
     * Handler for the /paradisu warp delete command
     *
     * @param context the data specified on registration of the command
     */
    private void deleteWarpCommand(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.sender().getSender();
        WarpModel warp = this.localWarp(sender, context.get("name"));
        if (warp == null) {
            return;
        }

        this.reply(
                sender,
                warp.name(),
                paradisu.warpManager().deleteWarp(warp.id()),
                paradisu.messagesConfig()
                        .commands()
                        .paradisu()
                        .warp()
                        .delete()
                        .output()
                        .get(0));
    }

    /**
     * Handler for the /paradisu warp permission command. Sets the permission required to use a warp, or clears it if
     * none is given.
     *
     * @param context the data specified on registration of the command
     */
    private void permissionWarpCommand(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.sender().getSender();
        WarpModel warp = this.localWarp(sender, context.get("name"));
        if (warp == null) {
            return;
        }

        String permission = context.getOrDefault("permission", null);
        Paradisu.Warp.Permission pText =
                paradisu.messagesConfig().commands().paradisu().warp().permission();
        this.reply(
                sender,
                warp.name(),
                paradisu.warpManager().updateWarp(warp.id(), managed -> managed.permission(permission)),
                permission == null ? pText.output().get(1) : pText.output().get(0),
                Placeholder.unparsed("permission", permission == null ? "" : permission));
    }

    /**
     * Handler for the /paradisu warp aliases command. Replaces a warp's aliases with the given space-separated list, or
     * clears them if none are given.
     *
     * @param context the data specified on registration of the command
     */
    private void aliasesWarpCommand(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.sender().getSender();
        WarpModel warp = this.localWarp(sender, context.get("name"));
        if (warp == null) {
            return;
        }

        String input = context.getOrDefault("aliases", "");
        String[] aliases = new LinkedHashSet<>(Arrays.asList(input.trim().split("\\s+")))
                .stream().filter(alias -> !alias.isBlank()).toArray(String[]::new);
        Paradisu.Warp.Aliases aText =
                paradisu.messagesConfig().commands().paradisu().warp().aliases();
        this.reply(
                sender,
                warp.name(),
                paradisu.warpManager().updateWarp(warp.id(), managed -> managed.aliases(aliases)),
                aliases.length == 0 ? aText.output().get(1) : aText.output().get(0),
                Placeholder.unparsed("aliases", String.join(", ", aliases)));
    }

    /**
     * Looks up a warp in this server's warp context, telling the sender if it does not exist.
     *
     * @param sender the command sender
     * @param name the warp name
     * @return the warp, or null if it does not exist
     */
    private WarpModel localWarp(CommandSender sender, String name) {
        WarpModel warp =
                paradisu.warpManager().exactWarp(name, paradisu.warpManager().localContext());
        if (warp == null) {
            Messages.sendPrefixed(
                    sender,
                    paradisu.messagesConfig()
                            .commands()
                            .paradisu()
                            .warp()
                            .output()
                            .get(0),
                    Placeholder.unparsed("warp", name),
                    Placeholder.unparsed("context", paradisu.warpManager().localContext()));
        }
        return warp;
    }

    /**
     * Sends the success or failure message once a warp change completes. {@link WarpManager} completes the future on
     * the main thread.
     *
     * @param sender the command sender
     * @param name the warp name
     * @param result the pending change
     * @param success the message template sent on success
     * @param extra additional placeholders for the success message
     */
    private void reply(
            CommandSender sender,
            String name,
            CompletableFuture<Boolean> result,
            String success,
            TagResolver... extra) {
        result.thenAccept(saved -> {
            if (saved) {
                Messages.sendPrefixed(
                        sender,
                        success,
                        TagResolver.resolver(Placeholder.unparsed("warp", name), TagResolver.resolver(extra)));
            } else {
                Messages.sendPrefixed(
                        sender,
                        paradisu.messagesConfig()
                                .commands()
                                .paradisu()
                                .warp()
                                .output()
                                .get(2),
                        Placeholder.unparsed("warp", name));
            }
        });
    }
}
