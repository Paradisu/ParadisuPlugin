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

public final class TeleportCommand extends AbstractCommand {
    public TeleportCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("vtp", "vteleport")
            .permission("vparadisu.vtp")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vtp")
            .argument(PlayerArgument.of("target"), ArgumentDescription.of("paradisu.command.help.vtp.0"))
            .argument(PlayerArgument.optional("player"), ArgumentDescription.of("paradisu.command.help.vtp.1"))
            .handler(this::teleportCommand);
        this.commandManager.command(builder);
    }
    
    @SuppressWarnings("unchecked")
    private void teleportCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.getOrDefault("player", context.getSender());

        paradisu.getConnector().getBridge().teleport(player.getUsername(), target.getUsername(), m -> {})
        .whenComplete((success, exception) -> {
            if (!success) {
                paradisu.logger().error("Error teleporting: " + exception.getMessage());
            }
        });
        context.getSender().sendMessage(
            Messages.prefixed(
                Component.translatable()
                    .key("paradisu.command.output.vtp")
                    .args(
                        Component.text(player.getUsername()).color(NamedTextColor.GOLD), 
                        Component.text(target.getUsername()).color(NamedTextColor.GOLD))
                    .build()
        ));
    }
}
