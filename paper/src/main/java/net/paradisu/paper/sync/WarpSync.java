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

package net.paradisu.paper.sync;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import org.bukkit.Bukkit;

import java.util.List;

@RequiredArgsConstructor
public class WarpSync implements Runnable {
    private final ParadisuPaper paradisu;

    @Override
    public void run() {
        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager(); ) {
            List<WarpModel> warps = entityManager
                    .createQuery("SELECT w FROM WarpModel w", WarpModel.class)
                    .getResultList();
            warps.forEach(w -> entityManager.detach(w));
            Bukkit.getScheduler().runTask(paradisu, () -> {
                paradisu.warpManager().setWarps(warps);
            });
        } catch (Exception e) {
            paradisu.logger().error("Failed to load warps from the database", e);
        }
    }
}
