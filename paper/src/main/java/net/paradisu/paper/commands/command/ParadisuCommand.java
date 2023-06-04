package net.paradisu.paper.commands.command;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.paradisu.core.locale.Messages;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import org.bukkit.command.CommandSender;

public final class ParadisuCommand extends AbstractPaperCommand {
    public ParadisuCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("paradisu")
            .meta(CommandMeta.DESCRIPTION, paradisu.messagesConfig().commands().paradisu().helpMsg()
            );

        this.commandManager.command(builder.literal("help")
        .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
        .handler(context -> {
            this.helpManager.getMinecraftHelp().queryCommands(context.getOrDefault("query", ""), context.getSender());
        }));

        this.commandManager.command(builder.literal("about")
            .permission("paradisu.about")
            .meta(CommandMeta.DESCRIPTION, paradisu.messagesConfig().commands().paradisu().about().helpMsg())
            .handler(this::aboutCommand)
        );

        this.commandManager.command(builder.literal("reload")
            .permission("paradisu.reload")
            .meta(CommandMeta.DESCRIPTION, paradisu.messagesConfig().commands().paradisu().reload().helpMsg())
            .handler(this::reloadCommand)
        );
    }

    /**
     * Handeler for the /paradisu about command
     * @param context the data specified on registration of the command
     */
    private void aboutCommand(CommandContext<CommandSender> context) {
        context.getSender().sendMessage(Messages.prefixed(
            MiniMessage.miniMessage().deserialize(
                paradisu.messagesConfig().commands().paradisu().about().output().get(0)
            )
        ));
    }

    /**
     * Handeler for the /paradisu reload command
     * @param context the data specified on registration of the command
     */
    private void reloadCommand(CommandContext<CommandSender> context) {
        paradisu.reload();
        context.getSender().sendMessage(Messages.prefixed(
                MiniMessage.miniMessage().deserialize(paradisu.messagesConfig().commands().paradisu().reload().output().get(0))
        ));
    }
}
