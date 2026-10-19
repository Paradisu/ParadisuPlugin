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

package net.paradisu.paper.items;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.paradisu.database.models.ItemModel;
import net.paradisu.paper.ParadisuPaper;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/** Caches detached custom models for the model menu. */
@RequiredArgsConstructor
public class ModelManager {
    private final ParadisuPaper paradisu;
    private volatile List<ItemModel> models = List.of();
    private volatile boolean loaded;
    private volatile List<String> tags = List.of();
    private CompletableFuture<Boolean> reload;

    /** Returns the last successfully loaded model snapshot. */
    public List<ItemModel> models() {
        return models;
    }

    /** Returns cached models whose item_use matches the tag, ignoring case; blank means all models. */
    public List<ItemModel> models(String tag) {
        if (tag == null || tag.isBlank()) {
            return models;
        }
        String search = tag.trim();
        return models.stream()
                .filter(model ->
                        model.item_use() != null && model.item_use().trim().equalsIgnoreCase(search))
                .toList();
    }

    /** Returns distinct cached item_use tags for command completion without database requests. */
    public List<String> tags() {
        return tags;
    }

    /** Returns whether an initial model snapshot is available. */
    public boolean loaded() {
        return loaded;
    }

    /**
     * Refreshes models asynchronously, retaining the previous snapshot on failure. Call on the main thread. Concurrent
     * callers share one refresh.
     *
     * @return a future completed on the main thread with whether loading succeeded
     */
    public CompletableFuture<Boolean> syncModels() {
        if (reload != null && !reload.isDone()) {
            return reload;
        }
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        reload = result;
        Bukkit.getScheduler().runTaskAsynchronously(paradisu, () -> {
            List<ItemModel> snapshot;
            try (EntityManager em = paradisu.databaseSession().factory().createEntityManager()) {
                snapshot = List.copyOf(
                        em.createQuery("SELECT i FROM ItemModel i ORDER BY i.identifier ASC", ItemModel.class)
                                .getResultList());
                snapshot.forEach(em::detach);
            } catch (Exception e) {
                paradisu.logger().error("Failed to load custom models from the database", e);
                Bukkit.getScheduler().runTask(paradisu, () -> result.complete(false));
                return;
            }
            Bukkit.getScheduler().runTask(paradisu, () -> {
                tags = snapshot.stream()
                        .map(ItemModel::item_use)
                        .filter(tag -> tag != null && !tag.isBlank())
                        .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                        .distinct()
                        .sorted()
                        .toList();
                models = snapshot;
                loaded = true;
                result.complete(true);
            });
        });
        return result;
    }
}
