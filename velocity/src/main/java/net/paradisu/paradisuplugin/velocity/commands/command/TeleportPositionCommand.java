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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class TeleportPositionCommand extends AbstractCommand {
    public TeleportPositionCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tppos", "tpposition")
            .permission("vparadisu.tppos")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().tppos().helpMsg())
            .argument(DoubleArgument.of("x"), ArgumentDescription.of(paradisu.commands().tppos().helpArgs(0)))
            .argument(DoubleArgument.of("y"), ArgumentDescription.of(paradisu.commands().tppos().helpArgs(1)))
            .argument(DoubleArgument.of("z"), ArgumentDescription.of(paradisu.commands().tppos().helpArgs(2)))
            .argument(ServerArgument.optional("server"), ArgumentDescription.of(paradisu.commands().tppos().helpArgs(3)))
            .argument(PlayerArgument.optional("player"), ArgumentDescription.of(paradisu.commands().tppos().helpArgs(4)))
            .handler(this::teleportPositionCommand);
        this.commandManager.command(builder);
    }
    
    /**
     * Handeler for the /tppos command
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
                        context.getSender().sendMessage(Messages.prefixed(
                                MiniMessage.miniMessage().deserialize(
                                    paradisu.commands().tppos().output(0),
                                    Placeholder.component("player", Component.text(player.getUsername())),
                                    Placeholder.component("posx", Component.text((int) telportLocation.getX())),
                                    Placeholder.component("posy", Component.text((int) telportLocation.getY())),
                                    Placeholder.component("posz", Component.text((int) telportLocation.getZ())),
                                    Placeholder.component("server", Component.text(telportLocation.getServer()))
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
