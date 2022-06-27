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

public final class TeleportHereCommand extends AbstractCommand {
    public TeleportHereCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("vtphere", "vteleporthere")
            .permission("vparadisu.vtphere")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vtphere")
            .argument(PlayerArgument.of("target"), ArgumentDescription.of("paradisu.command.help.vtphere.0"))
            .handler(this::teleportCommand);
        this.commandManager.command(builder);
    }
    
    /**
     * Handeler for the /vtphere command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.getSender();

        paradisu.getConnector().getBridge().teleport(target.getUsername(), player.getUsername(), m -> {})
        .whenComplete((success, exception) -> {
            if (success) {
                context.getSender().sendMessage(
                    Messages.prefixed(
                        Component.translatable()
                            .key("paradisu.command.output.vtphere")
                            .args(
                                Component.text(target.getUsername()).color(NamedTextColor.GOLD))
                            .build()
                ));
            } else {
                paradisu.logger().error("Error teleporting: " + exception.getMessage());
            }
        });
    }
}
