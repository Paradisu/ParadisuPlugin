package net.paradisu.velocity.commands.command;

import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;

public final class TeleportHereCommand extends AbstractVelocityCommand {
    public TeleportHereCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tph", "tphere")
            .permission("vparadisu.tph")
            .commandDescription(Description.of(paradisu.messagesConfig().commands().tph().helpMsg()))
            .required("target", PlayerParser.playerParser(), Description.of(paradisu.messagesConfig().commands().tph().helpArgs().get(0)))
            .handler(this::teleportCommand);
        this.commandManager.command(builder);
    }
    
    /**
     * Handeler for the /tph command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player target = (Player) context.get("target");
        Player player = (Player) context.sender();

        paradisu.connector().getBridge().getLocation(target)
        .whenComplete((location, locationException) -> {
            if (locationException == null) {
                history.addTeleport(target, location);
                paradisu.connector().getBridge().teleport(target.getUsername(), player.getUsername(), m -> {})
                .whenComplete((success, teleportException) -> {
                    if (success) {
                        player.sendMessage(
                            Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                paradisu.messagesConfig().commands().tph().output().get(0),
                                Placeholder.component("player", Component.text(target.getUsername()))
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
    }
}
