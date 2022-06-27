package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import cloud.commandframework.velocity.arguments.ServerArgument;
import de.themoep.connectorplugin.LocationInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class TeleportPositionCommand extends AbstractCommand {
    public TeleportPositionCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("vtppos", "vtpposition")
            .permission("vparadisu.vtppos")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vtppos")
            .argument(DoubleArgument.of("x"), ArgumentDescription.of("paradisu.command.help.vtppos.0"))
            .argument(DoubleArgument.of("y"), ArgumentDescription.of("paradisu.command.help.vtppos.1"))
            .argument(DoubleArgument.of("z"), ArgumentDescription.of("paradisu.command.help.vtppos.2"))
            .argument(ServerArgument.optional("server"), ArgumentDescription.of("paradisu.command.help.vtppos.3"))
            .argument(PlayerArgument.optional("player"), ArgumentDescription.of("paradisu.command.help.vtppos.4"))
            .handler(this::teleportPositionCommand);
        this.commandManager.command(builder);
    }
    
    /**
     * Handeler for the /vtppos command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportPositionCommand(CommandContext<CommandSource> context) {
        Player player = (Player) context.getOrDefault("player", context.getSender());
        paradisu.getConnector().getBridge().getLocation(player)
        .whenComplete((location, locationException) -> {
            if (locationException == null) {
                RegisteredServer server = context.getOrDefault("server", null);
                LocationInfo telportLocation = new LocationInfo(
                    server == null ?  location.getServer() : server.getServerInfo().getName(),
                    location.getWorld(),
                    context.get("x"),
                    context.get("y"),
                    context.get("z"));
                paradisu.getConnector().getBridge().teleport(player.getUsername(), telportLocation, m -> {})
                .whenComplete((success, teleportException) -> {
                    if (success) {
                        context.getSender().sendMessage(
                            Messages.prefixed(
                                Component.translatable()
                                    .key("paradisu.command.output.vtppos")
                                    .args(
                                        Component.text(player.getUsername()).color(NamedTextColor.GOLD), 
                                        Component.text((int) telportLocation.getX()).color(NamedTextColor.GOLD), 
                                        Component.text((int) telportLocation.getY()).color(NamedTextColor.GOLD), 
                                        Component.text((int) telportLocation.getZ()).color(NamedTextColor.GOLD),
                                        Component.text(telportLocation.getServer()).color(NamedTextColor.GOLD))
                                    .build()
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
