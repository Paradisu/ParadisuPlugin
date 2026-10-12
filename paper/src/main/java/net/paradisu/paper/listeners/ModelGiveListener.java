/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
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

package net.paradisu.paper.listeners;

import net.paradisu.database.models.ItemModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.inventory.ModelGiveInventory;
import net.paradisu.paper.util.ModelItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class ModelGiveListener implements Listener {

    private final ParadisuPaper paradisu;
    private final ModelItemFactory factory;

    public ModelGiveListener(ParadisuPaper paradisu) {
        this.paradisu = paradisu;
        this.factory = new ModelItemFactory(paradisu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getView().getTopInventory().getHolder() instanceof ModelGiveInventory menu)) {
            return;
        }

        // Never allow modification of the GUI.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!player.hasPermission("paradisu.mgive")) {
            return;
        }

        // Only ordinary left clicks perform actions.
        if (event.getClick() != ClickType.LEFT) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot == ModelGiveInventory.PREVIOUS_SLOT) {
            if (menu.page() > 0) {
                ModelGiveInventory.openPage(paradisu, player, menu.page() - 1);
            }
            return;
        }

        if (slot == ModelGiveInventory.NEXT_SLOT) {
            if (menu.page() + 1 < menu.totalPages()) {
                ModelGiveInventory.openPage(paradisu, player, menu.page() + 1);
            }
            return;
        }

        // Only the first 45 slots contain models.
        if (slot < 0 || slot >= ModelGiveInventory.PAGE_SIZE) {
            return;
        }

        ItemModel model = menu.modelAt(slot);

        if (model == null) {
            return;
        }

        ItemStack item = factory.createItem(model);

        if (item == null) {
            player.sendMessage("This model has an invalid item type.");
            return;
        }

        // Give one copy without touching the GUI item.
        var leftover = player.getInventory().addItem(item);

        if (!leftover.isEmpty()) {
            player.sendMessage("Your inventory is full.");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (event.getView().getTopInventory().getHolder() instanceof ModelGiveInventory) {
            event.setCancelled(true);
        }
    }
}
