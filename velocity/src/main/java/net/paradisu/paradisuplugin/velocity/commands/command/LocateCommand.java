package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.locate")
            .argument(PlayerArgument.of("player"), ArgumentDescription.of("paradisu.command.help.locate.0"))
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
                context.getSender().sendMessage(
                    Messages.prefixed(
                        Component.translatable()
                            .key("paradisu.command.output.locate")
                            .args(
                                Component.text(player.getUsername()).color(NamedTextColor.GOLD), 
                                Component.text(location.getServer()).color(NamedTextColor.GOLD), 
                                Component.text((int) location.getX()).color(NamedTextColor.GOLD), 
                                Component.text((int) location.getY()).color(NamedTextColor.GOLD), 
                                Component.text((int) location.getZ()).color(NamedTextColor.GOLD))
                            .build()
                ));
            } else {
                paradisu.logger().error("Error getting location: " + exception.getMessage());
            }
        });
    }
}
