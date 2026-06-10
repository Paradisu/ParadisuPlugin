package net.paradisu.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.themoep.connectorplugin.LocationInfo;
import jakarta.persistence.EntityManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.database.models.playerdata.PlayerModel;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.util.teleport.TeleportHistory;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.velocity.parser.PlayerParser;

public class BalanceCommand extends AbstractVelocityCommand {
    public BalanceCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("balance", "bal")
                .permission("vparadisu.balance")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().balance().helpMsg()))
                .optional(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .tp()
                                .helpArgs()
                                .get(0)))
                .handler(this::balanceCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /back command
     *
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void balanceCommand(CommandContext<CommandSource> context) {
        Player player = (Player) context.getOrDefault("player", context.sender());
        long balance = 0;
        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager()) {
            PlayerModel playerModel = entityManager.find(PlayerModel.class, player.getUniqueId());

            if(playerModel == null) {
                return;
            }

            balance = playerModel.balance();
        }

        context.sender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(
                                paradisu.messagesConfig()
                                        .commands()
                                        .balance()
                                        .output()
                                        .get(0),
                                Placeholder.component(
                                        "player", Component.text(player.getUsername())
                                ),
                                Placeholder.component(
                                        "balance", Component.text(balance)))));
    }
}
