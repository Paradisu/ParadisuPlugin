package me.jakedadream.snwplugin.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel1() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aWarp Pipe");
        meta.setUnbreakable(true);
        item.setDurability((short)1);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel2() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a1/2 Warp Pipe");
        meta.setUnbreakable(true);
        item.setDurability((short)2);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel3() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7Spawn Lights");
        meta.setUnbreakable(true);
        item.setDurability((short)3);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel4() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cRed §fLollipop");
        meta.setUnbreakable(true);
        item.setDurability((short)4);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel5() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cPirahna §aPlant §fUrinal");
        meta.setUnbreakable(true);
        item.setDurability((short)5);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel6() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eStar §cPost");
        meta.setUnbreakable(true);
        item.setDurability((short)6);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel7() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§4Ribbon");
        meta.setUnbreakable(true);
        item.setDurability((short)7);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel8() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dPeach's Castle §bStained Glass Window");
        meta.setUnbreakable(true);
        item.setDurability((short)8);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel9() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 9);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§lNint Sign");
        meta.setUnbreakable(true);
        item.setDurability((short)9);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel10() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 10);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§lSuper Sign");
        meta.setUnbreakable(true);
        item.setDurability((short)10);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel11() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 11);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§lEndo Sign");
        meta.setUnbreakable(true);
        item.setDurability((short)11);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel12() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 12);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§lWorld Sign");
        meta.setUnbreakable(true);
        item.setDurability((short)12);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel13() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 13);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§lJake's §7§lLong §8§lSword");
        meta.setUnbreakable(true);
        item.setDurability((short)13);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel14() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 14);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lQuestion Block");
        meta.setUnbreakable(true);
        item.setDurability((short)14);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel15() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 15);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bPeach's Castle §eWindow");
        meta.setUnbreakable(true);
        item.setDurability((short)15);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel16() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 16);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Pirahana Plant Spike");
        meta.setUnbreakable(true);
        item.setDurability((short)16);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel17() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 17);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6(NOT CURRENCY) §eGold Coin Prop");
        meta.setUnbreakable(true);
        item.setDurability((short)17);
        item.setItemMeta(meta)
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createmodel18() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 18);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lQUESTION MARK COIN");
        meta.setUnbreakable(true);
        item.setDurability((short)18);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel19() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 19);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3§lSNW Light Post #1 (Bottom Half)");
        meta.setUnbreakable(true);
        item.setDurability((short)19);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel20() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 20);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3§lSNW Light Post #1 (Top Half)");
        meta.setUnbreakable(true);
        item.setDurability((short)20);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel21() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 21);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§4Piranha Plant Main (Aero)");
        meta.setUnbreakable(true);
        item.setDurability((short)21);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel22() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 22);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§4Piranha Plant Jaw (Aero)");
        meta.setUnbreakable(true);
        item.setDurability((short)22);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel23() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 23);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aGreen §2Shell");
        meta.setUnbreakable(true);
        item.setDurability((short)23);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel24() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 24);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§716 Bit §c§lMario §9§lCap");
        meta.setUnbreakable(true);
        item.setDurability((short)24);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel25() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 25);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3[§cBuilder§3] §e§lHard Hat");
        meta.setUnbreakable(true);
        item.setDurability((short)25);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel26() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 26);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§716 Bit §a§lLuigi §9§lCap");
        meta.setUnbreakable(true);
        item.setDurability((short)26);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel27() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 27);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dPeach's §eCrown");
        meta.setUnbreakable(true);
        item.setDurability((short)27);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel28() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 28);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3Sailor §bMoons §ehair");
        meta.setUnbreakable(true);
        item.setDurability((short)28);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel29() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 29);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§lShark §f§lCap");
        meta.setUnbreakable(true);
        item.setDurability((short)29);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel30() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 30);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3Jaws Boat§fFront");
        meta.setUnbreakable(true);
        item.setDurability((short)30);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel31() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 31);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3Jaws Boat§fBack");
        meta.setUnbreakable(true);
        item.setDurability((short)31);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel32() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 32);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c&lMushroom §f§lTable §7(Medium)");
        meta.setUnbreakable(true);
        item.setDurability((short)32);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel33() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 33);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c&lMushroom §f§lTable §7(Small)");
        meta.setUnbreakable(true);
        item.setDurability((short)33);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel34() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 34);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c&lMushroom §f§lTable §7(Large)");
        meta.setUnbreakable(true);
        item.setDurability((short)34);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel35() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 35);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§lClouds §f☻");
        meta.setUnbreakable(true);
        item.setDurability((short)35);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel36() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 36);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§lBullet §7Bill");
        meta.setUnbreakable(true);
        item.setDurability((short)36);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel37() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 37);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a3 Way Warp Pipe");
        meta.setUnbreakable(true);
        item.setDurability((short)37);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel38() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 38);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cMario §eFlag");
        meta.setUnbreakable(true);
        item.setDurability((short)38);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel39() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 39);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cF§6i§er§ce §6F§el§co§6w§ee§cr");
        meta.setUnbreakable(true);
        item.setDurability((short)39);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel40() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 40);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lKey §e§lCoin");
        meta.setUnbreakable(true);
        item.setDurability((short)40);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel41() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 41);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eGoodbye §b20§f20 §eGlasses");
        meta.setUnbreakable(true);
        item.setDurability((short)41);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel42() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 42);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§lDarksaber");
        meta.setUnbreakable(true);
        item.setDurability((short)42);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel43() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 43);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lPOW! §4§lBlock");
        meta.setUnbreakable(true);
        item.setDurability((short)43);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel44() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 44);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lPOW! §3§lBlock");
        meta.setUnbreakable(true);
        item.setDurability((short)44);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel45() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 45);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§lJaws §f§lDisplay Shark");
        meta.setUnbreakable(true);
        item.setDurability((short)45);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel46() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 46);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§lBROKEN MODEL");
        meta.setUnbreakable(true);
        item.setDurability((short)46);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel47() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 47);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§fFull White Fence");
        meta.setUnbreakable(true);
        item.setDurability((short)47);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel48() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 48);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§fHalf White Fence");
        meta.setUnbreakable(true);
        item.setDurability((short)48);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel49() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 49);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§lBullet §7Bill Cannon");
        meta.setUnbreakable(true);
        item.setDurability((short)49);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel50() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 50);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lMario §9§lWristband");
        meta.setUnbreakable(true);
        item.setDurability((short)50);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel51() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 51);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lLuigi §9§lWristband");
        meta.setUnbreakable(true);
        item.setDurability((short)1);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel52() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 52);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§Yoshi's §f§lEgg");
        meta.setUnbreakable(true);
        item.setDurability((short)1);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel53() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 53);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§Yoshi");
        meta.setUnbreakable(true);
        item.setDurability((short)1);
        item.setItemMeta(meta)
        return item;
    }
    @SuppressWarnings("deprecation")
    public static ItemStack createmodel54() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 54);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§Thwomp!");
        meta.setUnbreakable(true);
        item.setDurability((short)1);
        item.setItemMeta(meta)
        return item;
    }
    public static ItemStack createmodel55() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 55);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lMario's §9§lCap §fStrawberry Shortcake sandwich");
        List<String> lore = new ArrayList<>();
        lore.add("§f--------------------");
        lore.add("§aRight click to eat!");
        lore.add("§6(Effects you with Speed)");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createmodel56() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 56);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lLuigi's §9§lCap §fNo-Bake Cheesecake sandwhich");
        List<String> lore = new ArrayList<>();
        lore.add("§f--------------------");
        lore.add("§aRight click to eat!");
        lore.add("§6(Effects you with Speed)");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createmodel57() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 57);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lMario's §f§lStrawberry Soda");
        List<String> lore = new ArrayList<>();
        lore.add("§f--------------------");
        lore.add("§aRight click to Drink!");
        lore.add("§6(Effects you with Speed)");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createmodel58() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 58);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lLuigi's §f§lGreen Apple Soda");
        List<String> lore = new ArrayList<>();
        lore.add("§f--------------------");
        lore.add("§aRight click to Drink!");
        lore.add("§6(Effects you with Speed)");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createmodel59() {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1, 59);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lPrincess Peach's §f§lPeach Soda");
        List<String> lore = new ArrayList<>();
        lore.add("§f--------------------");
        lore.add("§aRight click to Drink!");
        lore.add("§6(Effects you with Speed)");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
}

