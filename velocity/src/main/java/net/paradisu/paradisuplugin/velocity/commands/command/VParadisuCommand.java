package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class VParadisuCommand extends AbstractCommand {
    public VParadisuCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        

        var builder = this.commandManager.commandBuilder("vparadisu")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vparadisu"
            );

        this.commandManager.command(builder.literal("help")
        .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
        .handler(context -> {
            this.minecraftHelp.queryCommands(context.getOrDefault("query", ""), context.getSender());
        })
        );
        
        this.commandManager.command(builder.literal("about")
            .permission("vparadisu.about")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vparadisu.about")
            .handler(this::aboutCommand)
        );

        this.commandManager.command(builder.literal("reload")
            .permission("vparadisu.reload")
            .meta(CommandMeta.DESCRIPTION, "paradisu.command.help.vparadisu.reload")
            .handler(this::reloadCommand)
        );
    }

    private void aboutCommand(CommandContext<CommandSource> context) {
        context.getSender().sendMessage(Messages.prefixed(Messages.COMMAND_OUTPUT_VPARADISU_ABOUT));
    }

    private void reloadCommand(CommandContext<CommandSource> context) {
        this.paradisu.reload();
        context.getSender().sendMessage(Messages.prefixed(Messages.COMMAND_OUTPUT_VPARADISU_RELOAD));
    }
}
