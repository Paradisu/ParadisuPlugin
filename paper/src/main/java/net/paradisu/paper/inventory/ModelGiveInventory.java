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

package net.paradisu.paper.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.core.locale.Messages;
import net.paradisu.database.models.ItemModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.util.ModelItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ModelGiveInventory implements InventoryHolder {

    public static final int PAGE_SIZE = 36;
    public static final int PREVIOUS_SLOT = 45;
    public static final int PAGE_SLOT = 49;
    public static final int NEXT_SLOT = 53;

    private final Inventory inventory;
    private final List<ItemModel> models;
    private final int page;
    private final String tag;
    private final String query;
    private final int totalPages;

    public ModelGiveInventory(
            int page, int totalPages, List<ItemModel> models, ModelItemFactory factory, String tag, String query) {

        this.page = page;
        this.tag = tag;
        this.query = query;
        this.totalPages = totalPages;
        this.models = List.copyOf(models);

        this.inventory =
                Bukkit.createInventory(this, 54, Component.text("Models - Page " + (page + 1) + "/" + totalPages));

        // Populate four rows of models, leaving the fifth row empty above navigation.
        for (int i = 0; i < models.size(); i++) {
            inventory.setItem(i, factory.createPreview(models.get(i)));
        }

        // Fill the navigation row.
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, button(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        if (page > 0) {
            inventory.setItem(PREVIOUS_SLOT, modelButton(28, "Previous Page"));
        }

        inventory.setItem(PAGE_SLOT, modelButton(160, "Page " + (page + 1) + "/" + totalPages));

        if (page + 1 < totalPages) {
            inventory.setItem(NEXT_SLOT, modelButton(48, "Next Page"));
        }
    }

    private ItemStack modelButton(float modelData, String name) {
        ItemStack item = button(Material.DIAMOND_AXE, name);
        ItemMeta meta = item.getItemMeta();
        var data = meta.getCustomModelDataComponent();
        data.setFloats(List.of(modelData));
        meta.setCustomModelDataComponent(data);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name, NamedTextColor.YELLOW));

        item.setItemMeta(meta);
        return item;
    }

    public ItemModel modelAt(int slot) {
        if (slot < 0 || slot >= models.size()) {
            return null;
        }
        return models.get(slot);
    }

    public int page() {
        return page;
    }

    /** Returns the item_use filter carried across menu pages. */
    public String tag() {
        return tag;
    }

    /** Returns the name search carried across menu pages. */
    public String query() {
        return query;
    }

    public int totalPages() {
        return totalPages;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /** Opens a page of all cached models. */
    public static void openPage(ParadisuPaper paradisu, Player player, int requestedPage) {
        openPage(paradisu, player, requestedPage, "");
    }

    /** Opens a page of cached models filtered by their item_use tag. */
    public static void openPage(ParadisuPaper paradisu, Player player, int requestedPage, String tag) {
        openPage(paradisu, player, requestedPage, tag, "");
    }

    /** Opens a page filtered by tag or, when supplied, a model display name search. */
    public static void openPage(ParadisuPaper paradisu, Player player, int requestedPage, String tag, String query) {
        openPage(paradisu, player, requestedPage, tag, query, false);
    }

    /** Opens a model page and optionally confirms the command and its filter. */
    public static void openPage(
            ParadisuPaper paradisu, Player player, int requestedPage, String tag, String query, boolean announce) {

        if (!player.hasPermission("paradisu.mgive")) {
            return;
        }

        if (!paradisu.modelManager().loaded()) {
            Messages.sendPrefixed(
                    player,
                    paradisu.messagesConfig().commands().mgive().output().get(0));
            return;
        }
        List<ItemModel> allModels = query.isBlank()
                ? paradisu.modelManager().models(tag)
                : paradisu.modelManager().search(query);
        if (allModels.isEmpty() && !query.isBlank()) {
            Messages.sendPrefixed(
                    player,
                    paradisu.messagesConfig().commands().mgive().searchNoMatches(),
                    Placeholder.component("query", Component.text(query, TextColor.color(0xFFA500))));
            return;
        }
        if (allModels.isEmpty() && !tag.isBlank()) {
            Messages.sendPrefixed(
                    player,
                    paradisu.messagesConfig().commands().mgive().noMatches(),
                    Placeholder.component("tag", Component.text(tag, TextColor.color(0xFFA500))));
            return;
        }
        int totalPages = Math.max(1, (allModels.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int start = page * PAGE_SIZE;
        List<ItemModel> models = allModels.subList(start, Math.min(start + PAGE_SIZE, allModels.size()));
        ModelGiveInventory menu =
                new ModelGiveInventory(page, totalPages, models, new ModelItemFactory(paradisu), tag, query);
        player.openInventory(menu.getInventory());
        if (announce) {
            var messages = paradisu.messagesConfig().commands().mgive();
            String message = !query.isBlank()
                    ? messages.openedSearch()
                    : !tag.isBlank() ? messages.openedTag() : messages.opened();
            Messages.sendPrefixed(
                    player,
                    message,
                    Placeholder.component("tag", Component.text(tag, TextColor.color(0xFFA500))),
                    Placeholder.component("query", Component.text(query, TextColor.color(0xFFA500))));
        }
    }
}
