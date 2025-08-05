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

package net.paradisu.paper.warps;

import de.themoep.connectorplugin.connector.MessageTarget;
import jakarta.persistence.EntityManager;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.messaging.messages.SyncWarpsMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WarpManager {
    private final ParadisuPaper paradisu;
    private List<WarpModel> warps;

    private Map<WarpKey, WarpModel> warpMap;
    private Map<String, List<WarpModel>> nameWarpMap = new HashMap<>();

    public WarpManager(ParadisuPaper paradisu) {
        this.paradisu = paradisu;
        this.warpMap = new HashMap<>();
        this.nameWarpMap = new HashMap<>();
        this.syncWarps();
    }

    public boolean syncWarps() {
        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager(); ) {
            this.warps = entityManager
                    .createQuery("SELECT w FROM WarpModel w", WarpModel.class)
                    .getResultList();
            this.warps.forEach(w -> entityManager.detach(w));
            this.warpMap = this.warps.stream()
                    .collect(Collectors.toMap(warp -> new WarpKey(warp.name(), warp.context()), warp -> warp));
            this.nameWarpMap = this.warps.stream().collect(Collectors.groupingBy(WarpModel::name, Collectors.toList()));
            return true;
        } catch (Exception e) {
            paradisu.logger().error("Failed to load warps from the database", e);
            return false;
        }
    }

    public CompletableFuture<Boolean> createWarp(WarpModel warp) {
        return CompletableFuture.supplyAsync(() -> {
            try (EntityManager entityManager =
                    paradisu.databaseSession().factory().createEntityManager(); ) {
                entityManager.getTransaction().begin();
                entityManager.persist(warp);
                entityManager.getTransaction().commit();
                paradisu.messagingManager().send(MessageTarget.ALL_QUEUE, new SyncWarpsMessage.Message());
                return true;
            } catch (Exception e) {
                paradisu.logger().error("Failed to create warp in the database", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> deleteWarp(String warpName) {
        return CompletableFuture.supplyAsync(() -> {
            try (EntityManager entityManager =
                    paradisu.databaseSession().factory().createEntityManager(); ) {
                entityManager.getTransaction().begin();
                WarpModel warp = entityManager.find(WarpModel.class, warpName);
                if (warp != null) {
                    entityManager.remove(warp);
                    entityManager.getTransaction().commit();
                    paradisu.messagingManager().send(MessageTarget.ALL_QUEUE, new SyncWarpsMessage.Message());
                    return true;
                } else {
                    paradisu.logger().warn("Warp " + warpName + " not found for deletion");
                    return false;
                }
            } catch (Exception e) {
                paradisu.logger().error("Failed to delete warp from the database", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> updateWarp(WarpModel warp) {
        return CompletableFuture.supplyAsync(() -> {
            try (EntityManager entityManager =
                    paradisu.databaseSession().factory().createEntityManager(); ) {
                entityManager.getTransaction().begin();
                WarpModel existingWarp = entityManager.find(WarpModel.class, warp.name());
                if (existingWarp != null) {
                    existingWarp.permission(warp.permission());
                    existingWarp.x(warp.x());
                    existingWarp.y(warp.y());
                    existingWarp.z(warp.z());
                    existingWarp.yaw(warp.yaw());
                    existingWarp.pitch(warp.pitch());
                    entityManager.getTransaction().commit();
                    paradisu.messagingManager().send(MessageTarget.ALL_QUEUE, new SyncWarpsMessage.Message());
                    return true;
                } else {
                    paradisu.logger().warn("Warp " + warp.name() + " not found for update");
                    return false;
                }
            } catch (Exception e) {
                paradisu.logger().error("Failed to update warp in the database", e);
                return false;
            }
        });
    }

    public WarpModel getClosestWarp(String name) {
        List<WarpModel> warps = this.nameWarpMap.get(name);
        if (warps.isEmpty()) {
            return null;
        }
        if (warps.size() == 1) {
            return warps.getFirst();
        }

        String context = paradisu.paradisuConfig().context().warp();
        return warps.stream()
                .filter(warp -> warp.context().equals(context))
                .findFirst()
                .orElseGet(() -> warps.getFirst());
    }

    public WarpModel getExactWarp(String name, String context) {
        return this.warpMap.get(new WarpKey(name, context));
    }

    record WarpKey(String name, String context) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WarpKey warpKey = (WarpKey) o;
            return name.equals(warpKey.name) && context.equals(warpKey.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, context);
        }
    }
}
