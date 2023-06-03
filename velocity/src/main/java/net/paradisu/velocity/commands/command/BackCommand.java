package net.paradisu.velocity.commands.command;

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
import net.paradisu.core.locale.Messages;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;

public final class BackCommand extends AbstractVelocityCommand {
    public BackCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("back", "return")
            .permission("vparadisu.back")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().back().helpMsg())
            .argument(PlayerArgument.optional("player"), ArgumentDescription.of(paradisu.commands().back().helpArgs().get(0)))
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
            paradisu.connector().getBridge().getLocation(player)
            .whenComplete((location, locationException) -> {
                if (locationException == null) {
                    history.addTeleport(player, location);
                    paradisu.connector().getBridge().teleport(player.getUsername(), previousLocation, m -> {})
                    .whenComplete((success, teleportException) -> {
                        if (success) {
                            context.getSender().sendMessage(
                                Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                    paradisu.commands().back().output().get(0),
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
                    paradisu.commands().back().output().get(1),
                    Placeholder.component("player", Component.text(player.getUsername()))
                )
            ));
        }
    }
}
