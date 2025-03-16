package net.paradisu.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportQueue;
import net.paradisu.velocity.commands.util.teleport.TeleportRequestHeader;

public final class TeleportDenyCommand extends AbstractVelocityCommand {
    public TeleportDenyCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tpd", "tpdeny")
            .permission("vparadisu.tpa")
            .commandDescription(Description.of(paradisu.messagesConfig().commands().tpd().helpMsg()))
            .optional("target", PlayerParser.playerParser(), Description.of(paradisu.messagesConfig().commands().tpd().helpArgs().get(0)))
            .handler(this::teleportDenyCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tpa command
     * @param context the data specified on registration of the command
     */
    private void teleportDenyCommand(CommandContext<CommandSource> context) {
        TeleportQueue queue = new TeleportQueue();
        
        Player sender = (Player) context.sender();
        Player target = (Player) context.getOrDefault("target", queue.getRecentTeleport(sender));

        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(target, sender);
        
        boolean existingRequest = queue.isTeleportQueued(requestHeader);

        if (existingRequest) {
            queue.removeTeleport(requestHeader);

            target.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.messagesConfig().commands().tpd().output().get(0),
                    Placeholder.component("player", Component.text(sender.getUsername()))
                )
            ));

            sender.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.messagesConfig().commands().tpd().output().get(1),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        } else {
            if (context.getOrDefault("target", null) == null) {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage().deserialize(paradisu.messagesConfig().commands().tpd().output().get(2))));
            } else {
                sender.sendMessage(Messages.prefixed(
                    MiniMessage.miniMessage().deserialize(
                        paradisu.messagesConfig().commands().tpd().output().get(3),
                        Placeholder.component("player", Component.text(target.getUsername()))
                    )
                ));
            }
        }
    }
}
