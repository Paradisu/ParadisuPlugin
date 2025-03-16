package net.paradisu.paper.commands.command;

import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.standard.StringParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.paradisu.core.locale.Messages;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;

public final class ParadisuCommand extends AbstractPaperCommand {
    public ParadisuCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("paradisu")
            .commandDescription(Description.of(paradisu.messagesConfig().commands().paradisu().helpMsg()));

        this.commandManager.command(builder.literal("help")
            .optional("query", StringParser.greedyStringParser())
            .handler(context -> {
                this.helpManager.getMinecraftHelp().queryCommands(context.getOrDefault("query", ""), context.sender());
        }));

        this.commandManager.command(builder.literal("about")
            .permission("paradisu.about")
            .commandDescription(CommandDescription.commandDescription(paradisu.messagesConfig().commands().paradisu().about().helpMsg()))
            .handler(this::aboutCommand)
        );

        this.commandManager.command(builder.literal("reload")
            .permission("paradisu.reload")
            .commandDescription(CommandDescription.commandDescription(paradisu.messagesConfig().commands().paradisu().reload().helpMsg()))
            .handler(this::reloadCommand)
        );
    }

    /**
     * Handeler for the /paradisu about command
     * @param context the data specified on registration of the command
     */
    private void aboutCommand(CommandContext<CommandSourceStack> context) {
        context.sender().getSender().sendMessage(Messages.prefixed(
            MiniMessage.miniMessage().deserialize(
                paradisu.messagesConfig().commands().paradisu().about().output().get(0)
            )
        ));
    }

    /**
     * Handeler for the /paradisu reload command
     * @param context the data specified on registration of the command
     */
    private void reloadCommand(CommandContext<CommandSourceStack> context) {
        paradisu.reload();
        context.sender().getSender().sendMessage(Messages.prefixed(
                MiniMessage.miniMessage().deserialize(paradisu.messagesConfig().commands().paradisu().reload().output().get(0))
        ));
    }
}
