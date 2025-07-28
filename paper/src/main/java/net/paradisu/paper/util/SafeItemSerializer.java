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
        if (item == null) {
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
