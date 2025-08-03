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

package net.paradisu.database;

public class Models {
    public static Class<?>[] models() {
        return new Class<?>[] {
            net.paradisu.database.models.playerdata.PlayerModel.class,
            net.paradisu.database.models.playerdata.PlayerServerSessionModel.class,
            net.paradisu.database.models.playerdata.PlayerProxySessionModel.class,
            net.paradisu.database.models.playerdata.PlayerInventoryModel.class,
            net.paradisu.database.models.WarpModel.class
        };
    }
}
