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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.paradisu.core.locale.Messages;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.commands.AbstractPaperCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

public class HatCommand extends AbstractPaperCommand {
    public HatCommand(ParadisuPaper paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager
                .commandBuilder("hat")
                .permission("paradisu.hat")
                .commandDescription(Description.of(
                        paradisu.messagesConfig().commands().hat().helpMsg()))
                .handler(this::hatCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handler for the /hat command Replaces the player's current helmet with the item in their main hand.
     *
     * @param context the data specified on registration of the command
     */
    private void hatCommand(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.sender().getSender();
        ItemStack currentHelmet = player.getInventory().getHelmet();
        ItemStack handItem = player.getInventory().getItemInMainHand().clone();
        if (currentHelmet != null) {
            currentHelmet = currentHelmet.clone();
        }
        player.getInventory().setHelmet(handItem);
        player.getInventory().setItemInMainHand(currentHelmet);
        player.sendMessage(Messages.prefixed(MiniMessage.miniMessage()
                .deserialize(paradisu.messagesConfig().commands().hat().output().get(0))));

    }
}
