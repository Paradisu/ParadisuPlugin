package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.commands.util.TeleportQueue;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class TeleportAcceptCommand extends AbstractCommand {
    public TeleportAcceptCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("vtpa", "vtpaccept")
            .permission("vparadisu.vtpa")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vtpa")
            .handler(this::teleportAcceptCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /vtp command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportAcceptCommand(CommandContext<CommandSource> context) {
        TeleportQueue queue = new TeleportQueue();
        
        Player sender = (Player) context.getSender();
        Player teleportingPlayer = (Player) queue.getPlayer(sender, 0);
        Player stationaryPlayer = (Player) queue.getPlayer(sender, 1);
        
        boolean validRequest = (teleportingPlayer != null && stationaryPlayer != null);
        queue.removeTeleport(sender);

        if (validRequest) {
            paradisu.getConnector().getBridge().teleport(teleportingPlayer.getUsername(), stationaryPlayer.getUsername(), m -> {})
            .whenComplete((success, exception) -> {
                if (success) {
                    teleportingPlayer.sendMessage(
                        Messages.prefixed(
                            Component.translatable()
                                .key("paradisu.command.output.vtpa.0")
                                .args(
                                    Component.text(stationaryPlayer.getUsername()).color(NamedTextColor.GOLD))
                                .build()
                    ));
                    stationaryPlayer.sendMessage(
                        Messages.prefixed(
                            Component.translatable()
                                .key("paradisu.command.output.vtpa.1")
                                .args(
                                    Component.text(teleportingPlayer.getUsername()).color(NamedTextColor.GOLD))
                                .build()
                    ));
                } else {
                    paradisu.logger().error("Error teleporting: " + exception.getMessage());
                }
            });
        } else {
            context.getSender().sendMessage(Messages.prefixed(Messages.COMMAND_OUTPUT_VTPA_2));
        }
    }
}
