package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import de.themoep.connectorplugin.LocationInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.commands.util.teleport.TeleportHistory;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class BackCommand extends AbstractCommand {
    public BackCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("back", "return")
            .permission("vparadisu.back")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().back().helpMsg())
            .argument(PlayerArgument.optional("player"), ArgumentDescription.of(paradisu.commands().back().helpArgs(0)))
            .handler(this::backCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /back command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void backCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player player = (Player) context.getOrDefault("player", context.getSender());
        LocationInfo previousLocation = history.getTeleport(player);
        boolean validRequest = (previousLocation != null);

        if (validRequest) {
            paradisu.getConnector().getBridge().getLocation(player)
            .whenComplete((location, locationException) -> {
                if (locationException == null) {
                    history.addTeleport(player, location);
                    paradisu.getConnector().getBridge().teleport(player.getUsername(), previousLocation, m -> {})
                    .whenComplete((success, teleportException) -> {
                        if (success) {
                            context.getSender().sendMessage(
                                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                    paradisu.commands().back().output(0),
                                    Placeholder.component("player", Component.text(player.getUsername()))
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
        } else {
            context.getSender().sendMessage(
                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                    paradisu.commands().back().output(1),
                    Placeholder.component("player", Component.text(player.getUsername()))
                )
            ));
        }
    }
}
