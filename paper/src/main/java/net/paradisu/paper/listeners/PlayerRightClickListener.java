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

package net.paradisu.paper.listeners;

import lombok.AllArgsConstructor;
import net.paradisu.paper.ParadisuPaper;

import net.paradisu.paper.items.ItemsBlindbox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class PlayerRightClickListener implements Listener {
    private final ParadisuPaper paradisu;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // SAFETY CHECKS
        if(item == null || item.getType().isAir()) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) { // prevents double opens
            return;
        }
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack blindbox = ItemsBlindbox.createBlindbox();

        // BLIND BOXES
                if(item.isSimilar(blindbox)) {
                    event.setCancelled(true);
                    int rolledNumber = ThreadLocalRandom.current().nextInt(1, 9);
                    ItemStack rewardMinion = ItemsBlindbox.minionBlindBoxReward(String.valueOf(rolledNumber));

                    if (item.getAmount() > 1) {
                        // Decrease blindbox stack size by one
                        item.setAmount(item.getAmount() - 1);
                        // Put reward directly into inventory
                        player.getInventory().addItem(rewardMinion);

                    } else {
                        // Fully replace single item in hand
                        player.getInventory().setItem(event.getHand(), rewardMinion);
                }

        } // if it is the correct item

        // WANDS
            // if(meta == wands.getItemMeta())

    }
}
