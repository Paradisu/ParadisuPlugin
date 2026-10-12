
package net.paradisu.paper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
                paradisu.getLogger().warning(
                        "Could not deserialize item data for model "
                                + model.identifier());
            }
        }

        ItemMeta meta = item.getItemMeta();

        if (model.display_name() != null
                && !model.display_name().isBlank()) {
            meta.displayName(Component.text(model.display_name()));
        }

        if (model.model_path() != null
                && !model.model_path().isBlank()) {
            NamespacedKey key =
                    NamespacedKey.fromString(model.model_path());

            if (key != null) {
                meta.setItemModel(key);
            } else {
                paradisu.getLogger().warning(
                        "Invalid model path: " + model.model_path());
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
            invalidMeta.displayName(
                    Component.text("Invalid Model", NamedTextColor.RED));
            item.setItemMeta(invalidMeta);
        }

        ItemMeta meta = item.getItemMeta();

        List<Component> lore = meta.lore() == null
                ? new ArrayList<>()
                : new ArrayList<>(meta.lore());

        lore.add(Component.empty());
        lore.add(Component.text(
                "ID: " + model.identifier(), NamedTextColor.GRAY));

        lore.add(Component.text(
                "Left-click to receive one", NamedTextColor.YELLOW));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
