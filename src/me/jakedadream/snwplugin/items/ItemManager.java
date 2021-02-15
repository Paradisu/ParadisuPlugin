package me.jakedadream.snwplugin.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemManager {

    public static ItemStack createCoin() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lGOLD COIN");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createThruWand() {
        ItemStack item = new ItemStack(Material.SPECTRAL_ARROW, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7'//Thru' §3Wand");
        List<String> lore = new ArrayList<>();
        lore.add("§dRight click wand to //thru");
        lore.add("§7(Requires worldedit perms)");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LUCK, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createLLamazBanWand() {
        ItemStack item = new ItemStack(Material.SPECTRAL_ARROW, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7'/ban Llamaz' §3Wand");
        List<String> lore = new ArrayList<>();
        lore.add("§cRight click wand to /ban Llamaz");
        lore.add("§7(Requires ban perms)");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LUCK, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private static String[] names = {
            "templateName", //0
            "§aWarp Pipe",
            "§aWarp Pipe",
            "§a1/2 Warp Pipe",
            "§7Spawn Lights",
            "§cRed §fLollipop",
            "§cPirahna §aPlant §fUrinal",
            "§eStar §cPost",
            "§4Ribbon",
            "§dPeach's Castle §bStained Glass Window",
            "§f§lNint Sign",
            "§f§lSuper Sign",
            "§f§lEndo Sign",
            "§f§lWorld Sign",
            "§f§lJake's §7§lLong §8§lSword",
            "§e§lQuestion Block",
            "§bPeach's Castle §eWindow",
            "§6Pirahana Plant Spike",
            "§6(NOT CURRENCY) §eGold Coin Prop",
            "§e§lQUESTION MARK COIN",
            "§3§lSNW Light Post #1 (Bottom Half)",
            "§3§lSNW Light Post #1 (Top Half)",
            "§4Piranha Plant Main (Aero)",
            "§4Piranha Plant Jaw (Aero)",
            "§aGreen §2Shell",
            "§716 Bit §c§lMario §9§lCap",
            "§3[§cBuilder§3] §e§lHard Hat",
            "§716 Bit §a§lLuigi §9§lCap",
            "§dPeach's §eCrown",
            "§3Sailor §bMoons §ehair",
            "§7§lShark §f§lCap",
            "§3Jaws Boat§fFront",
            "§3Jaws Boat§fBack",
            "§c&lMushroom §f§lTable §7(Medium)",
            "§c&lMushroom §f§lTable §7(Small)",
            "§c&lMushroom §f§lTable §7(Large)",
            "§f§lClouds §f☻",
            "§7§lBullet §7Bill",
            "§a3 Way Warp Pipe",
            "§cMario §eFlag",
            "§cF§6i§er§ce §6F§el§co§6w§ee§cr",
            "§6§lKey §e§lCoin",
            "§eGoodbye §b20§f20 §eGlasses",
            "§7§lDarksaber",
            "§6§lPOW! §4§lBlock",
            "§b§lPOW! §3§lBlock",
            "§7§lJaws §f§lDisplay Shark",
            "§7§lBROKEN MODEL",
            "§fFull White Fence",
            "§fHalf White Fence",
            "§7§lBullet §7Bill Cannon",
            "§c§lMario §9§lWristband",
            "§a§lLuigi §9§lWristband",
            "§a§Yoshi's §f§lEgg",
            "§a§Yoshi",
            "§7§Thwomp!",
            "§c§lMario's §9§lCap §fStrawberry Shortcake sandwich",
            "§a§lLuigi's §9§lCap §fNo-Bake Cheesecake sandwhich",
            "§c§lMario's §f§lStrawberry Soda",
            "§a§lLuigi's §f§lGreen Apple Soda",
            "§e§lPrincess Peach's §f§lPeach Soda",
            "§fZero §4Twos §cHeadband",
            "§aPalutena's §6Magic §3Staff"
    };

    private HashMap<Integer, String> lore;

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel(short id) {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(names[id]);
        meta.setUnbreakable(true);
        item.setDurability(id);
        item.setItemMeta(meta);
        return item;
    }
}