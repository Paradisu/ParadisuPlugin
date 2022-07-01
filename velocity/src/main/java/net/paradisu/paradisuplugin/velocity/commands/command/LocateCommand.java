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
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class LocateCommand extends AbstractCommand {
    public LocateCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("locate", "locateplayer", "find", "findplayer")
            .permission("vparadisu.locate")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().locate().helpMsg())
            .argument(PlayerArgument.of("player"), ArgumentDescription.of(paradisu.commands().locate().helpArgs(0)))
            .handler(this::locateCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /locate command
     * @param context the data specified on registration of the command
     */
    private void locateCommand(CommandContext<CommandSource> context) {
        Player player = (Player) context.get("player");

        paradisu.getConnector().getBridge().getLocation(player)
        .whenComplete((location, exception) -> {
            if (exception == null) {
                context.getSender().sendMessage(Messages.prefixed(
                    MiniMessage.miniMessage().deserialize(
                        paradisu.commands().locate().output(0),
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
