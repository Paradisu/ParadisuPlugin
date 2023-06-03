package net.paradisu.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
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

public final class TeleportRequestCommand extends AbstractVelocityCommand {
    public TeleportRequestCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tpr", "tprequest")
            .permission("vparadisu.tpr")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().tpr().helpMsg())
            .argument(PlayerArgument.of("target"), ArgumentDescription.of(paradisu.commands().tpr().helpArgs().get(0)))
            .handler(this::teleportRequestCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tpr command
     * @param context the data specified on registration of the command
     */
    private void teleportRequestCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.getSender();
        Player[] teleportArray = {player, target};

        TeleportQueue queue = new TeleportQueue();
        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(player, target);

        boolean existingRequest = queue.isTeleportQueued(requestHeader, teleportArray);

        if (!existingRequest) {
            queue.queueTeleport(requestHeader, teleportArray);

            String acceptCommand = "/tpa " + player.getUsername();
            target.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpr().output().get(0),
                    Placeholder.component("player", Component.text(player.getUsername())),
                    Placeholder.component("command", Component.text(acceptCommand)
                        .clickEvent(ClickEvent.runCommand(acceptCommand))
                        .hoverEvent(Component.text(acceptCommand).color(NamedTextColor.GREEN)))
                )
            ));

            player.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpr().output().get(1),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        } else {
            player.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpr().output().get(2),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        }
    }
}
