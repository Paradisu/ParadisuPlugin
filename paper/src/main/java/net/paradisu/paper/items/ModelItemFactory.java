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

package net.paradisu.paper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.paradisu.database.models.ItemModel;
import net.paradisu.paper.ParadisuPaper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ModelItemFactory {

    private final ParadisuPaper paradisu;

    public ModelItemFactory(ParadisuPaper paradisu) {
        this.paradisu = paradisu;
    }

    /**
     * Creates an item with optional saved NBT. A numeric model_path overrides the first custom model data float; a
     * namespaced model_path selects an item model definition instead.
     *
     * @param model the cached model row
     * @return the item, or null if its material or model path is invalid
     */
    public ItemStack createItem(ItemModel model) {
        if (model.item_type() == null) {
            return null;
        }

        Material material = Material.matchMaterial(model.item_type());

        if (material == null || material.isAir()) {
            return null;
        }

        ItemStack item = new ItemStack(material, 1);

        // Supports NBT only if saved using Bukkit's
        // ItemStack.serializeAsBytes() format.
        if (model.nbt() != null && model.nbt().length > 0) {
            try {
                ItemStack saved = ItemStack.deserializeBytes(model.nbt());

                if (saved.getType() == material) {
                    item = saved;
                    item.setAmount(1);
                }
            } catch (Exception ex) {
                paradisu.logger().warn("Could not deserialize item data for model " + model.identifier());
            }
        }

        ItemMeta meta = item.getItemMeta();

        if (model.display_name() != null && !model.display_name().isBlank()) {
            meta.displayName(MiniMessage.miniMessage()
                    .deserialize(model.display_name())
                    .decoration(TextDecoration.ITALIC, false));
        }

        if (model.model_path() != null && !model.model_path().isBlank()) {
            String path = model.model_path().trim();
            try {
                float value = Float.parseFloat(path);
                if (!Float.isFinite(value)) {
                    throw new NumberFormatException("Non-finite custom model data");
                }
                var data = meta.getCustomModelDataComponent();
                data.setFloats(List.of(value));
                meta.setCustomModelDataComponent(data);
                meta.setItemModel(null);
            } catch (NumberFormatException e) {
                NamespacedKey key = path.contains(":") ? NamespacedKey.fromString(path) : null;
                if (key == null) {
                    paradisu.logger().warn("Invalid model path for model " + model.identifier() + ": " + path);
                    return null;
                }
                meta.setItemModel(key);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createPreview(ItemModel model) {
        ItemStack item = createItem(model);

        if (item == null) {
            item = new ItemStack(Material.BARRIER);
            ItemMeta invalidMeta = item.getItemMeta();
            invalidMeta.displayName(Component.text("Invalid Model", NamedTextColor.RED));
            item.setItemMeta(invalidMeta);
        }

        ItemMeta meta = item.getItemMeta();

        List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());

        lore.add(Component.empty());
        lore.add(Component.text("ID: " + model.identifier(), NamedTextColor.GRAY));

        lore.add(MiniMessage.miniMessage()
                .deserialize(paradisu.messagesConfig().commands().mgive().receiveHint()));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
