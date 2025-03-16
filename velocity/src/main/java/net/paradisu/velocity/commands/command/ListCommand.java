package net.paradisu.velocity.commands.command;

import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class ListCommand extends AbstractVelocityCommand {
    public ListCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("ls", "list")
            .permission("vparadisu.list")
            .commandDescription(Description.of(paradisu.messagesConfig().commands().ls().helpMsg()))
            .handler(this::listCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /ls command
     * @param context the data specified on registration of the command
     */
    private void listCommand(CommandContext<CommandSource> context) {
        // Luckperms API is needed to resolve meta
        LuckPerms luckPerms = LuckPermsProvider.get();

        // Map players and meta.playerlist-index.#
        Map<Integer, List<Player>> onlinePlayerPrefixes = paradisu.server().getAllPlayers().stream().collect(
            Collectors.groupingBy(
                (player) -> 
                    luckPerms.getPlayerAdapter(Player.class).getUser(player).getCachedData().getMetaData().getMetaValue("playerlist-index",Integer::parseInt).orElse(100)
            ));
        // Sort onlinePlayerPrefixes by playerlist-index keys in ascending order
        Map<Integer, List<Player>> onlinePlayerPrefixesSorted = new TreeMap<Integer, List<Player>>(onlinePlayerPrefixes);

        // Create a wraper for the TextComponent so it can be accessed in forEach
        var textComponentWrapper = new Object(){List<TextComponent> textComponent = new ArrayList<>();};

        // Create a TextComponent for each playerlist-index key
        onlinePlayerPrefixesSorted.forEach((index, players) -> {
            textComponentWrapper.textComponent.add(
                Component.text()
                    .append(MiniMessage.miniMessage().deserialize(paradisu.messagesConfig().utility().listPrefixes(index)))
                    .append(Component.text(players.stream().map(Player::getUsername).collect(Collectors.joining(", "))))
                    .append(Component.newline())
                    .build());
        });

        // Build and send the TextComponent to the player
        int playerCount = paradisu.server().getPlayerCount();
        context.sender().sendMessage(
            Component.text()
                .append(MiniMessage.miniMessage().deserialize(paradisu.messagesConfig().utility().messageDivider()))
                .append(Component.newline())
                .append(Messages.prefixed(
                    MiniMessage.miniMessage().deserialize(
                        paradisu.messagesConfig().commands().ls().output().get(Math.min(playerCount, 2)),
                        Placeholder.component("count", Component.text(playerCount)))))
                .append(textComponentWrapper.textComponent)
                .append(MiniMessage.miniMessage().deserialize(paradisu.messagesConfig().utility().messageDivider()))
                .build());
    }
}
