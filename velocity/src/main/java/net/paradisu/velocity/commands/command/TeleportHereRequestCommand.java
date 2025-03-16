package net.paradisu.velocity.commands.command;

import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;
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

public final class TeleportHereRequestCommand extends AbstractVelocityCommand {
    public TeleportHereRequestCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tprh", "tprhere")
            .permission("vparadisu.tprh")
            .commandDescription(Description.of(paradisu.messagesConfig().commands().tprh().helpMsg()))
            .required("target", PlayerParser.playerParser(), Description.of(paradisu.messagesConfig().commands().tprh().helpArgs().get(0)))
            .handler(this::teleportHereRequestCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tprh command
     * @param context the data specified on registration of the command
     */
    private void teleportHereRequestCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.sender();
        Player[] teleportArray = {target, player};

        TeleportQueue queue = new TeleportQueue();
        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(player, target);

        boolean existingRequest = queue.isTeleportQueued(requestHeader, teleportArray);

        if (!existingRequest) {
            queue.queueTeleport(requestHeader, teleportArray);

            String acceptCommand = "/tpa " + player.getUsername();
            target.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.messagesConfig().commands().tprh().output().get(0),
                    Placeholder.component("player", Component.text(player.getUsername())),
                    Placeholder.component("command", Component.text(acceptCommand)
                        .clickEvent(ClickEvent.runCommand(acceptCommand))
                        .hoverEvent(Component.text(acceptCommand).color(NamedTextColor.GREEN)))
                )
            ));

            player.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.messagesConfig().commands().tprh().output().get(1),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        } else {
            player.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.messagesConfig().commands().tprh().output().get(2),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        }
    }
}
