
package net.paradisu.paper.inventory;

import jakarta.persistence.EntityManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.logging.Level;

public class ModelGiveInventory implements InventoryHolder {

    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS_SLOT = 45;
    public static final int PAGE_SLOT = 49;
    public static final int NEXT_SLOT = 53;

    private final Inventory inventory;
    private final List<ItemModel> models;
    private final int page;
    private final int totalPages;

    public ModelGiveInventory(
            int page,
            int totalPages,
            List<ItemModel> models,
            ModelItemFactory factory) {

        this.page = page;
        this.totalPages = totalPages;
        this.models = List.copyOf(models);

        this.inventory = Bukkit.createInventory(
                this,
                54,
                Component.text("Models - Page "
                        + (page + 1) + "/" + totalPages));

        // Populate the first 45 slots.
        for (int i = 0; i < models.size(); i++) {
            inventory.setItem(
                    i, factory.createPreview(models.get(i)));
        }

        // Fill the navigation row.
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, button(
                    Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        if (page > 0) {
            inventory.setItem(PREVIOUS_SLOT,
                    button(Material.ARROW, "Previous Page"));
        }

        inventory.setItem(PAGE_SLOT,
                button(Material.PAPER,
                        "Page " + (page + 1) + "/" + totalPages));

        if (page + 1 < totalPages) {
            inventory.setItem(NEXT_SLOT,
                    button(Material.ARROW, "Next Page"));
        }
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(
                name, NamedTextColor.YELLOW));

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

    public int totalPages() {
        return totalPages;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void openPage(
            ParadisuPaper paradisu,
            Player player,
            int requestedPage) {

        if (!player.hasPermission("paradisu.mgive")) {
            return;
        }

        if (!paradisu.databaseSession().open()) {
            player.sendMessage("Database is unavailable.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(
                paradisu, () -> {

                    try (EntityManager em = paradisu
                            .databaseSession()
                            .factory()
                            .createEntityManager()) {

                        long total = em.createQuery(
                                "SELECT COUNT(i) FROM ItemModel i",
                                Long.class).getSingleResult();

                        int totalPages = (int) Math.max(
                                1, (total + PAGE_SIZE - 1) / PAGE_SIZE);

                        int page = Math.max(0,
                                Math.min(requestedPage, totalPages - 1));

                        List<ItemModel> models = em.createQuery(
                                        "SELECT i FROM ItemModel i "
                                                + "ORDER BY i.identifier ASC",
                                        ItemModel.class)
                                .setFirstResult(page * PAGE_SIZE)
                                .setMaxResults(PAGE_SIZE)
                                .getResultList();

                        Bukkit.getScheduler().runTask(paradisu, () -> {
                            if (!player.isOnline()) {
                                return;
                            }

                            ModelItemFactory factory =
                                    new ModelItemFactory(paradisu);

                            ModelGiveInventory menu =
                                    new ModelGiveInventory(
                                            page, totalPages, models, factory);

                            player.openInventory(menu.getInventory());
                        });

                    } catch (Exception ex) {
                        paradisu.getLogger().log(
                                Level.SEVERE, "Failed to load models", ex);

                        Bukkit.getScheduler().runTask(paradisu, () -> {
                            if (player.isOnline()) {
                                player.sendMessage(
                                        "Failed to load custom models.");
                            }
                        });
                    }
                });
    }
}
