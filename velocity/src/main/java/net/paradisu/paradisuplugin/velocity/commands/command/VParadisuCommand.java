package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;

import cloud.commandframework.context.CommandContext;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class VParadisuCommand extends AbstractCommand {
    public VParadisuCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("vparadisu");
        
        this.commandManager.command(builder.literal("about")
            .permission("vparadisu.about")
            .handler(this::aboutCommand)
        );

        this.commandManager.command(builder.literal("reload")
            .permission("vparadisu.reload")
            .handler(this::reloadCommand)
        );
    }

    private void aboutCommand(CommandContext<CommandSource> context) {
        context.getSender().sendMessage(Messages.prefixed(Messages.ABOUT));
    }

    private void reloadCommand(CommandContext<CommandSource> context) {
        this.paradisu.reload();
        context.getSender().sendMessage(Messages.prefixed(Messages.RELOAD));
    }
}
