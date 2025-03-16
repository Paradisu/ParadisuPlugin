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

public final class TeleportCommand extends AbstractVelocityCommand {
    public TeleportCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tp", "vteleport")
            .permission("vparadisu.tp")
            .commandDescription(Description.of(paradisu.messagesConfig().commands().tp().helpMsg()))
            .required("target", PlayerParser.playerParser(), Description.of(paradisu.messagesConfig().commands().tp().helpArgs().get(0)))
            .optional("player", PlayerParser.playerParser(), Description.of(paradisu.messagesConfig().commands().tp().helpArgs().get(1)))
            .handler(this::teleportHereCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tp command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportHereCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player target = (Player) context.get("target");
        Player player = (Player) context.getOrDefault("player", context.sender());

        paradisu.connector().getBridge().getLocation(player)
        .whenComplete((location, locationException) -> {
            if (locationException == null) {
                history.addTeleport(player, location);
                paradisu.connector().getBridge().teleport(player.getUsername(), target.getUsername(), m -> {})
                .whenComplete((success, teleportException) -> {
                    if (success) {
                        context.sender().sendMessage(
                            Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                paradisu.messagesConfig().commands().tp().output().get(0),
                                Placeholder.component("player", Component.text(player.getUsername())),
                                Placeholder.component("target", Component.text(target.getUsername()))
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
