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

import net.paradisu.core.locale.Messages;
import net.paradisu.database.models.ItemModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.inventory.ModelGiveInventory;
import net.paradisu.paper.util.ModelItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
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

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getView().getTopInventory().getHolder() instanceof ModelGiveInventory menu)) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= topSize) {
            // Shift transfer can insert into the menu; collect-to-cursor can take its display items.
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!player.hasPermission("paradisu.mgive")) {
            return;
        }

        ClickType click = event.getClick();
        if (click != ClickType.LEFT
                && click != ClickType.RIGHT
                && click != ClickType.SHIFT_LEFT
                && click != ClickType.SHIFT_RIGHT) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot == ModelGiveInventory.PREVIOUS_SLOT) {
            if (menu.page() > 0) {
                openPageAfterClick(player, menu, menu.page() - 1);
            }
            return;
        }

        if (slot == ModelGiveInventory.NEXT_SLOT) {
            if (menu.page() + 1 < menu.totalPages()) {
                openPageAfterClick(player, menu, menu.page() + 1);
            }
            return;
        }

        // Only the model rows contain selectable items.
        if (slot < 0 || slot >= ModelGiveInventory.PAGE_SIZE) {
            return;
        }

        ItemModel model = menu.modelAt(slot);

        if (model == null) {
            return;
        }

        ItemStack item = factory.createItem(model);

        if (item == null) {
            Messages.sendPrefixed(
                    player,
                    paradisu.messagesConfig().commands().mgive().output().get(1));
            return;
        }

        if (click == ClickType.RIGHT) {
            ItemStack cursor = player.getItemOnCursor();
            if (cursor.getType().isAir()) {
                player.setItemOnCursor(item);
            } else if (cursor.isSimilar(item) && cursor.getAmount() < cursor.getMaxStackSize()) {
                ItemStack combined = cursor.clone();
                combined.setAmount(combined.getAmount() + 1);
                player.setItemOnCursor(combined);
            } else {
                Messages.sendPrefixed(
                        player, paradisu.messagesConfig().commands().mgive().cursorUnavailable());
            }
            return;
        }

        // Left and shift clicks give one copy without touching the GUI item.
        var leftover = player.getInventory().addItem(item);

        if (!leftover.isEmpty()) {
            Messages.sendPrefixed(
                    player,
                    paradisu.messagesConfig().commands().mgive().output().get(2));
        }
    }

    private void openPageAfterClick(Player player, ModelGiveInventory menu, int page) {
        Bukkit.getScheduler().runTask(paradisu, () -> {
            if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == menu) {
                ModelGiveInventory.openPage(paradisu, player, page, menu.tag(), menu.query());
            }
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (event.getView().getTopInventory().getHolder() instanceof ModelGiveInventory
                && event.getRawSlots().stream()
                        .anyMatch(
                                slot -> slot < event.getView().getTopInventory().getSize())) {
            event.setCancelled(true);
        }
    }
}
