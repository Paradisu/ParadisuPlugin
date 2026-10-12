
package net.paradisu.paper.commands.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import net.paradisu.paper.inventory.ModelGiveInventory;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

public class ModelGiveCommand extends AbstractPaperCommand {

    public ModelGiveCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("mgive")
                .permission("paradisu.mgive")
                .commandDescription(
                        Description.of("Browse and obtain custom models"))
                .handler(this::mgiveCommand);

        this.commandManager.command(builder);
    }

    private void mgiveCommand(
            CommandContext<CommandSourceStack> context) {

        if (!(context.sender().getSender() instanceof Player player)) {
            context.sender().getSender().sendMessage(
                    "Only players can use this command.");
            return;
        }

        ModelGiveInventory.openPage(paradisu, player, 0);
    }
}
