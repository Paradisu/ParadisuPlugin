package net.paradisu.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.velocity.Paradisu;
import net.paradisu.velocity.commands.util.AbstractCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportQueue;
import net.paradisu.velocity.commands.util.teleport.TeleportRequestHeader;
import net.paradisu.velocity.locale.Messages;

public final class TeleportDenyCommand extends AbstractCommand {
    public TeleportDenyCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tpd", "tpdeny")
            .permission("vparadisu.tpa")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().tpa().helpMsg())
            .argument(PlayerArgument.optional("target"), ArgumentDescription.of(paradisu.commands().tpd().helpArgs(0)))
            .handler(this::teleportDenyCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tpa command
     * @param context the data specified on registration of the command
     */
    private void teleportDenyCommand(CommandContext<CommandSource> context) {
        TeleportQueue queue = new TeleportQueue();
        
        Player sender = (Player) context.getSender();
        Player target = (Player) context.getOrDefault("target", queue.getRecentTeleport(sender));

        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(target, sender);
        
        boolean existingRequest = queue.isTeleportQueued(requestHeader);

        if (existingRequest) {
            queue.removeTeleport(requestHeader);

            target.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpd().output(0),
                    Placeholder.component("player", Component.text(sender.getUsername()))
                )
            ));

            sender.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpd().output(1),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        } else {
            if (context.getOrDefault("target", null) == null) {
                sender.sendMessage(Messages.prefixed(MiniMessage.miniMessage().deserialize(paradisu.commands().tpd().output(2))));
            } else {
                sender.sendMessage(Messages.prefixed(
                    MiniMessage.miniMessage().deserialize(
                        paradisu.commands().tpd().output(3),
                        Placeholder.component("player", Component.text(target.getUsername()))
                    )
                ));
            }
        }
    }
}
