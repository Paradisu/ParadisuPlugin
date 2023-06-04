package net.paradisu.velocity.commands.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;
import net.paradisu.velocity.commands.util.teleport.TeleportQueue;
import net.paradisu.velocity.commands.util.teleport.TeleportRequestHeader;

public final class TeleportAcceptCommand extends AbstractVelocityCommand {
    public TeleportAcceptCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tpa", "tpaccept")
            .permission("vparadisu.tpa")
            .meta(CommandMeta.DESCRIPTION, paradisu.messagesConfig().commands().tpa().helpMsg())
            .argument(PlayerArgument.optional("target"), ArgumentDescription.of(paradisu.messagesConfig().commands().tpa().helpArgs().get(0)))
            .handler(this::teleportAcceptCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tpa command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportAcceptCommand(CommandContext<CommandSource> context) {
        TeleportQueue queue = new TeleportQueue();
        TeleportHistory history = new TeleportHistory();
        
        Player sender = (Player) context.getSender();
        Player target = (Player) context.getOrDefault("target", queue.getRecentTeleport(sender));

        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(target, sender);

        Player teleportingPlayer = (Player) queue.getPlayer(requestHeader, 0);
        Player stationaryPlayer = (Player) queue.getPlayer(requestHeader, 1);
        
        boolean validRequest = (teleportingPlayer != null && stationaryPlayer != null);
        queue.removeTeleport(requestHeader);

        if (validRequest) {
            paradisu.connector().getBridge().getLocation(teleportingPlayer)
            .whenComplete((location, locationException) -> {
                if (locationException == null) {
                    history.addTeleport(teleportingPlayer, location);
                    if (teleportingPlayer == null || stationaryPlayer == null) {
                        return;
                    }
                    paradisu.connector().getBridge().teleport(teleportingPlayer.getUsername(), stationaryPlayer.getUsername(), m -> {})
                    .whenComplete((success, teleportException) -> {
                        if (success) {
                            teleportingPlayer.sendMessage(
                                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                    paradisu.messagesConfig().commands().tpa().output().get(0),
                                    Placeholder.component("player", Component.text(stationaryPlayer.getUsername()))
                                )
                            ));
                            stationaryPlayer.sendMessage(
                                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                    paradisu.messagesConfig().commands().tpa().output().get(1),
                                    Placeholder.component("player", Component.text(teleportingPlayer.getUsername()))
                                )
                            ));
                        } else {
                            paradisu.logger().error("Error teleporting: " + teleportException.getMessage());
                        }
                    });
                } else {
                    paradisu.logger().error("Error getting location: " + locationException.getMessage());
                }
            });
        } else {
            if (context.getOrDefault("target", null) == null) {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage().deserialize(paradisu.messagesConfig().commands().tpa().output().get(2))));
            } else {
                sender.sendMessage(Messages.prefixed(
                    MiniMessage.miniMessage().deserialize(
                        paradisu.messagesConfig().commands().tpa().output().get(3),
                        Placeholder.component("player", Component.text(target.getUsername()))
                    )
                ));
            }
        }
    }
}
