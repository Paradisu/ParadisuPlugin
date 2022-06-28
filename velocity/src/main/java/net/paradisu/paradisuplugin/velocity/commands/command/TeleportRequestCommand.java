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
import net.paradisu.paradisuplugin.velocity.commands.util.TeleportQueue;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class TeleportRequestCommand extends AbstractCommand {
    public TeleportRequestCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("vtpr", "vtprequest")
            .permission("vparadisu.vtpr")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vtpr")
            .flag(
                this.commandManager.flagBuilder("here")
                    .withAliases("h")
                    .withDescription(ArgumentDescription.of("paradisu.command.help.vtpr.0")))
            .argument(PlayerArgument.of("target"), ArgumentDescription.of("paradisu.command.help.vtpr.1"))
            .handler(this::teleportRequestCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /vtpr command
     * @param context the data specified on registration of the command
     */
    private void teleportRequestCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.getSender();
        boolean teleportHere = context.flags().isPresent("here");

        TeleportQueue queue = new TeleportQueue();
        queue.queueTeleport(target, teleportHere ? (new Player[] {target, player}) : (new Player[] {player, target}));

        target.sendMessage(
            Messages.prefixed(
                Component.translatable()
                    .key(teleportHere ? "paradisu.command.output.vtpr.0" : "paradisu.command.output.vtpr.1")
                    .args(
                        Component.text(player.getUsername()).color(NamedTextColor.GOLD))
                    .build()
        ));

        player.sendMessage(
            Messages.prefixed(
                Component.translatable()
                    .key(teleportHere ? "paradisu.command.output.vtpr.2" : "paradisu.command.output.vtpr.3")
                    .args(
                        Component.text(target.getUsername()).color(NamedTextColor.GOLD))
                    .build()
        ));
        

    }
}
