/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

        this.commandManager.command(builder.literal("lore").handler(this::openMetaDialogue));
    }

    public void openMetaDialogue(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.sender().getSender();
        String playerinput;

        Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Input your lore below"))
                        .inputs(List.of(DialogInput.numberRange(
                                        "experience", Component.text("Experience", NamedTextColor.GREEN), 0f, 100f)
                                .step(1f)
                                .initial(0f)
                                .labelFormat("%s: %s percent to the next level")
                                .width(300)
                                .build()))
                        .build()));
    }
}
