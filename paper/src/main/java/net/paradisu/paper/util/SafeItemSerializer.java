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

package net.paradisu.paper.util;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class SafeItemSerializer {
    private PaperLogger logger;

    /**
     * Serializes an array of ItemStacks to a byte array.
     *
     * @param items the array of ItemStacks to serialize
     * @return a byte array representing the serialized items or null if the input is null or empty
     */
    public ItemStack[] deserializeItemsFromBytes(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return ItemStack.deserializeItemsFromBytes(data);
        } catch (Exception e) {
            logger.error("Failed to deserialize items from bytes", e);
            return null;
        }
    }

    /**
     * Serializes a single ItemStack to a byte array.
     *
     * @param item the ItemStack to serialize
     * @return a byte array representing the serialized item or null if the input is null
     */
    public ItemStack deserializeBytes(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return ItemStack.deserializeBytes(data);
        } catch (Exception e) {
            logger.error("Failed to deserialize item from bytes", e);
            return null;
        }
    }

    /**
     * Serializes an array of ItemStacks to a byte array.
     *
     * @param items the array of ItemStacks to serialize
     * @return a byte array representing the serialized items or null if the input is null or empty
     */
    public byte[] serializeItemsAsBytes(ItemStack[] items) {
        if (items == null || items.length == 0) {
            return null;
        }
        try {
            return ItemStack.serializeItemsAsBytes(items);
        } catch (Exception e) {
            logger.error("Failed to serialize items", e);
            return null;
        }
    }

    /**
     * Serializes a single ItemStack to a byte array.
     *
     * @param item the ItemStack to serialize
     * @return a byte array representing the serialized item or null if the input is null
     */
    public byte[] serializeBytes(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        try {
            return item.serializeAsBytes();
        } catch (Exception e) {
            logger.error("Failed to serialize item", e);
            return null;
        }
    }
}
