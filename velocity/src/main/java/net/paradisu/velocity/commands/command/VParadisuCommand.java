package net.paradisu.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.paradisu.velocity.Paradisu;
import net.paradisu.velocity.commands.util.AbstractCommand;
import net.paradisu.velocity.locale.Messages;

public final class VParadisuCommand extends AbstractCommand {
    public VParadisuCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {

        var builder = this.commandManager.commandBuilder("vparadisu")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().vparadisu().helpMsg()
            );

        this.commandManager.command(builder.literal("help")
        .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
        .handler(context -> {
            this.minecraftHelp.queryCommands(context.getOrDefault("query", ""), context.getSender());
        }));
        
        this.commandManager.command(builder.literal("about")
            .permission("vparadisu.about")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().vparadisu().about().helpMsg())
            .handler(this::aboutCommand)
        );

        this.commandManager.command(builder.literal("reload")
            .permission("vparadisu.reload")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().vparadisu().reload().helpMsg())
            .handler(this::reloadCommand)
        );
    }

    /**
     * Handeler for the /vparadisu about command
     * @param context the data specified on registration of the command
     */
    private void aboutCommand(CommandContext<CommandSource> context) {
        context.getSender().sendMessage(Messages.prefixed(
            MiniMessage.miniMessage().deserialize(
                paradisu.commands().vparadisu().about().output(0)
            )
        ));
    }

    /**
     * Handeler for the /vparadisu reload command
     * @param context the data specified on registration of the command
     */
    private void reloadCommand(CommandContext<CommandSource> context) {
        paradisu.reload();
        context.getSender().sendMessage(Messages.prefixed(
                MiniMessage.miniMessage().deserialize(paradisu.commands().vparadisu().reload().output(0))
        ));
    }
}
