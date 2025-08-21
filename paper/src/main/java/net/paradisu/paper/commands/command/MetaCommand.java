package net.paradisu.paper.commands.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

import java.util.List;

public class MetaCommand extends AbstractPaperCommand {
    public MetaCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("meta")
                .permission("paradisu.meta")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().meta().helpMsg()));
        this.commandManager.command(builder);


        this.commandManager.command(builder.literal("lore")
                .handler(this::openMetaDialogue));
    }

    public void openMetaDialogue(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.sender().getSender();
        String playerinput;


        Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Input your lore below"))
                        .inputs(List.of(
                                DialogInput.numberRange("experience", Component.text("Experience", NamedTextColor.GREEN), 0f, 100f)
                                        .step(1f)
                                        .initial(0f)
                                        .labelFormat("%s: %s percent to the next level")
                                        .width(300)
                                        .build()
                        ))
                        .build()));


    }


}
