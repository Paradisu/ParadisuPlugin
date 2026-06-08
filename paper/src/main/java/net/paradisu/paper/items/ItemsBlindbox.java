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

package net.paradisu.paper.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class ItemsBlindbox {

    // TEMPORARY HARD CODE OF ITEM META (Name + Custom Model Data
    // Should it be in /resources below message_en.properties

    public static ItemStack createBlindbox() {
        ItemStack item = new ItemStack(Material.DIAMOND, 1);
        ItemMeta meta = item.getItemMeta();

        Component name = MiniMessage.miniMessage()
                        .deserialize("<gradient:#da95ed:#ed959e:#ed959e>Minions Zodiac Bind Box</gradient>");
        meta.displayName(name);

        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setFloats(List.of(1.0f));
        meta.setCustomModelDataComponent(cmd);

        return item;
    }

    public static ItemStack minionBlindBoxReward(String inputName) {
        ItemStack item = new ItemStack(Material.DIAMOND, 1);
        ItemMeta meta = item.getItemMeta();

        Component name = MiniMessage.miniMessage()
                .deserialize("<gradient:#da95ed:#ed959e:#ed959e>" + "Hello I'm Minion #" + inputName + "</gradient>");
        meta.displayName(name);

        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setFloats(List.of(1.0f));
        meta.setCustomModelDataComponent(cmd);

        return item;
    }

}
