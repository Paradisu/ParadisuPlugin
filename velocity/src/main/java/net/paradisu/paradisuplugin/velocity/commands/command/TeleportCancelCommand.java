package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.commands.util.teleport.TeleportQueue;
import net.paradisu.paradisuplugin.velocity.commands.util.teleport.TeleportRequestHeader;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class TeleportCancelCommand extends AbstractCommand {
    public TeleportCancelCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tpc", "tpcancel")
            .permission("vparadisu.tprh")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().tprh().helpMsg())
            .argument(PlayerArgument.of("target"), ArgumentDescription.of(paradisu.commands().tpc().helpArgs(0)))
            .handler(this::teleportCancelCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tprh command
     * @param context the data specified on registration of the command
     */
    private void teleportCancelCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.getSender();

        TeleportQueue queue = new TeleportQueue();
        TeleportRequestHeader requestHeader = new TeleportRequestHeader();
        requestHeader.setRequestHeader(player, target);

        boolean existingRequest = queue.isTeleportQueued(requestHeader);

        if (existingRequest) {
            queue.removeTeleport(requestHeader);

            target.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpc().output(0),
                    Placeholder.component("player", Component.text(player.getUsername()))
                )
            ));

            player.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpc().output(1),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        } else {
            player.sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().tpc().output(2),
                    Placeholder.component("player", Component.text(target.getUsername()))
                )
            ));
        }
    }
}
