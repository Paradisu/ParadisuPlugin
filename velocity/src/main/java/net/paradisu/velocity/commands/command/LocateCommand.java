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

public final class LocateCommand extends AbstractVelocityCommand {
    public LocateCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("locate", "locateplayer", "find", "findplayer")
            .permission("vparadisu.locate")
            .commandDescription(Description.of(paradisu.messagesConfig().commands().locate().helpMsg()))
            .required("player", PlayerParser.playerParser(), Description.of(paradisu.messagesConfig().commands().locate().helpArgs().get(0)))
            .handler(this::locateCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /locate command
     * @param context the data specified on registration of the command
     */
    private void locateCommand(CommandContext<CommandSource> context) {
        Player player = (Player) context.get("player");

        paradisu.connector().getBridge().getLocation(player)
        .whenComplete((location, exception) -> {
            if (exception == null) {
                context.sender().sendMessage(Messages.prefixed(
                    MiniMessage.miniMessage().deserialize(
                        paradisu.messagesConfig().commands().locate().output().get(0),
                        Placeholder.component("player", Component.text(player.getUsername())),
                        Placeholder.component("server", Component.text(location.getServer())),
                        Placeholder.component("posx", Component.text((int) location.getX())),
                        Placeholder.component("posy", Component.text((int) location.getY())),
                        Placeholder.component("posz", Component.text((int) location.getZ()))
                    )
            ));
            } else {
                paradisu.logger().error("Error getting location: " + exception.getMessage());
            }
        });
    }
}
