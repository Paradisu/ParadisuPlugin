package net.paradisu.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
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
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.velocity.parser.PlayerParser;

public final class SetBalanceCommand extends AbstractVelocityCommand {
    public SetBalanceCommand(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("setbalance", "setbal")
                .permission("vparadisu.setbalance")
                .commandDescription(
                        Description.of(paradisu.messagesConfig().commands().setBalance().helpMsg()))
                .required(
                        "player",
                        PlayerParser.playerParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .setBalance()
                                .helpArgs()
                                .get(0)))
                .required(
                        "newbalance",
                        StringParser.stringParser(),
                        Description.of(paradisu.messagesConfig()
                                .commands()
                                .setBalance()
                                .helpArgs()
                                .get(1)))
                .handler(this::setBalanceCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tp command
     *
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void setBalanceCommand(CommandContext<CommandSource> context) {
        Player player = (Player) context.get("player");
        String newbalance = context.get("newbalance");

        long balance = 0;
        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager()) {
            entityManager.getTransaction().begin();
            PlayerModel playerModel = entityManager.find(PlayerModel.class, player.getUniqueId());

            if (playerModel == null) {
                return;
            }

            balance = playerModel.balance();

                try {
                    if (newbalance.startsWith("+") || newbalance.startsWith("-")) {
                        long change = Long.parseLong(newbalance);
                        playerModel.balance(balance + change);
                    } else {
                        long setbalance = Long.parseLong(newbalance);
                        playerModel.balance(setbalance);
                    }
                } catch (NumberFormatException e) {
                    context.sender()
                            .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                                    .deserialize(
                                            paradisu.messagesConfig()
                                                    .commands()
                                                    .setBalance()
                                                    .output()
                                                    .get(1))));
                    return;
            }
            entityManager.getTransaction().commit();

            balance = playerModel.balance();
        }

        context.sender()
                .sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                        .deserialize(
                                paradisu.messagesConfig()
                                        .commands()
                                        .setBalance()
                                        .output()
                                        .get(0),
                                Placeholder.component(
                                        "player", Component.text(player.getUsername())),
                                Placeholder.component(
                                        "newbalance", Component.text(balance)))));
        }
    }
