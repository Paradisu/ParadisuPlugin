package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
//import java.util.concurrent.CompletableFuture;
//import de.themoep.connectorplugin.LocationInfo;

public final class LocateCommand extends AbstractCommand {
    public LocateCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("locate", "locateplayer", "find", "findplayer")
            .permission("vparadisu.locate")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.locate")
            .argument(PlayerArgument.of("player"))
            .handler(this::locateCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /locate command
     * @param context the data specified on registration of the command
     */
    private void locateCommand(CommandContext<CommandSource> context) {
        Player player = (Player) context.get("player");

        // Test just passing in a location to show that the parsing of the completable future works.
        //CompletableFuture<LocationInfo> test = new CompletableFuture<>();
        //LocationInfo testLocation = new LocationInfo("test", "test", 1, 2, 3);
        //test.complete(testLocation);
        //test

        // Log the bridge instance
        paradisu.logger().info("ConnectorPlugin instance: " + paradisu.getConnector().getBridge().toString());
        paradisu.logger().info("Player: " + player.getUsername() + " " + player.getUniqueId().toString());
        paradisu.logger().info("ConnectorPlugin server: " + paradisu.getConnector().getServerName());
        paradisu.getConnector().getBridge().getLocation(player)
        .whenComplete((location, exception) -> {
            if (exception != null) {
                paradisu.logger().error("Error getting location: " + exception.getMessage());
            } else {
                //debugging
                paradisu.logger().info(location.toString());
                double posX = location.getX();
                double posY = location.getY();
                double posZ = location.getZ();
                String server = location.getServer();
                context.getSender().sendMessage(Component.text("[" + posX + ", " + posY + ", " + posZ + "] " + server));
            }
        });
        //debugging
        context.getSender().sendMessage(Component.text("Command was executed"));
    }
}
