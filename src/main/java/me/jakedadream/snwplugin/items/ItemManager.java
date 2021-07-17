package me.jakedadream.snwplugin.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    public static ItemStack denybutton = createDenyButton();
    public static ItemStack acceptbutton = createAcceptButton();
    public static ItemStack blankbutton = createBlankButton();

        public static ItemStack createCoin() {
            ItemStack item = new ItemStack(Material.GOLD_NUGGET, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e§lGOLD COIN");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;
    }

        public static ItemStack createStarCoin() {
            ItemStack item = new ItemStack(Material.IRON_NUGGET, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§6✮ §f§k| §eSTAR COIN §f§k| §6✮");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;
    }

        private static ItemStack createAcceptButton() {
            ItemStack item = new ItemStack(Material.LIME_CONCRETE, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a§lAccept!");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;

    }
        private static ItemStack createDenyButton() {
            ItemStack item = new ItemStack(Material.RED_CONCRETE, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§c§lClose Menu!");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;

    }
        private static ItemStack createBlankButton() {
            ItemStack item = new ItemStack(Material.SUNFLOWER, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§7");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;

    }


    private static String[] names = {
            "templateName", //0
            "§aWarp Pipe", //1
            "§a1/2 Warp Pipe", //2
            "§7Spawn Lights", //3
            "§cRed §fLollipop", //4
            "§cPirahna §aPlant §fUrinal", //5
            "§eStar §cPost", //6
            "§4Ribbon", //7
            "§dPeach's Castle §bStained Glass Window", //8
            "§f§lNint Sign", //9
            "§f§lSuper Sign", //10
            "§f§lEndo Sign", //11
            "§f§lWorld Sign", //12
            "§f§lJake's §7§lLong §8§lSword", //13
            "§e§lQuestion Block", //14
            "§bPeach's Castle §eWindow", //15
            "§6Pirahana Plant Spike", //16
            "§6(NOT CURRENCY) §eGold Coin Prop", //17
            "§e§lQUESTION MARK COIN", //18
            "§3§lSNW Light Post #1 (Bottom Half)", //19
            "§3§lSNW Light Post #1 (Top Half)", //20
            "§4Piranha Plant Main (Aero)", //21
            "§4Piranha Plant Jaw (Aero)", //22
            "§aGreen §2Shell", //23
            "§716 Bit §c§lMario §9§lCap", //24
            "§3[§cBuilder§3] §e§lHard Hat", //25
            "§716 Bit §a§lLuigi §9§lCap", //26
            "§dPeach's §eCrown", //27
            "§3Sailor §bMoons §ehair", //28
            "§7§lShark §f§lCap", //29
            "§3Jaws Boat §fFront", //30
            "§3Jaws Boat §fBack", //31
            "§c§lMushroom §f§lTable §7(Medium)", //32
            "§c§lMushroom §f§lTable §7(Small)", //33
            "§c§lMushroom §f§lTable §7(Large)", //34
            "§f§lClouds §f☻", //35
            "§7§lBullet §7Bill", //36
            "§a3 Way Warp Pipe", //37
            "§cMario §eFlag", //38
            "§cF§6i§er§ce §6F§el§co§6w§ee§cr", //39
            "§6§lKey §e§lCoin", //40
            "§eGoodbye §b20§f20 §eGlasses", //41
            "§7§lDarksaber", //42
            "§6§lPOW! §4§lBlock", //43
            "§b§lPOW! §3§lBlock", //44
            "§7§lJaws §f§lDisplay Shark", //45
            "§7§lBROKEN MODEL", //46
            "§fFull White Fence", //47
            "§fHalf White Fence", //48
            "§7§lBullet §7Bill Cannon", //49
            "§c§lMario §9§lWristband", //50
            "§a§lLuigi §9§lWristband", //51
            "§a§lYoshi's §f§lEgg", //52
            "§a§lYoshi", //53
            "§7§lThwomp!", //54
            "§c§lMario's §9§lCap §fStrawberry Shortcake sandwich", //55
            "§a§lLuigi's §9§lCap §fNo-Bake Cheesecake sandwhich", //56
            "§c§lMario's §f§lStrawberry Soda", //57
            "§a§lLuigi's §f§lGreen Apple Soda", //58
            "§e§lPrincess Peach's §f§lPeach Soda", //59
            "§fZero §4Twos §cHeadband", //60
            "§aPalutena's §6Magic §3Staff", //61
            "§7ODM §2Gear", //62
            "§7ODM §2Gear §7Blade", //63
            "§eElizabeth §fthe §5§lGoomba", //64
            "§eBrad §fthe §c§lGoomba", //65
            "§eHissy §fthe §2§lGoomba", //66
            "§cPirahna §aPlant §fJaw", //67
            "§cPirahna §aPlant §fStem", //68
            "§cPirahna §aPlant §fMouth", //69
            "§fTex's §dSimp §6Compass", //70
            "§fSalty's §dSimp §6Compass", //71
            "§fUnconfirmable's §dSimp §6Compass", //72
            "§fEboy's §dSimp §6Compass", //73
            "§fJake's §dSimp §6Compass", //74
            "§4§lShy §f§lGuy", //75
            "§4§lWanda's Headband", //76
            "§0§lMais §8§lBunny Ears", //77
            "§6§lLlama Hat", //78
            "§b§lLight Blue §fYoshi Ride Cart", //79
            "§a§lLight Green §fYoshi Ride Cart", //80
            "§5§lPurple §fYoshi Ride Cart", //81
            "§6§lOrange §fYoshi Ride Cart", //82
            "§c§lRed §fYoshi Ride Cart", //83
            "§d§lPink §fYoshi Ride Cart", //84
            "§3§lBlue §fYoshi Ride Cart", //85
            "§e§lYellow §fYoshi Ride Cart", //86
            "§fYoshi Ride Cart §cBars", //87
            "§8§lBowser's Castle §7Recycle Can", //88
            "§8§lBowser's Castle §7Trash Can", //89
            "§3§lSuper Nintendo World §fRecycle Can", //90
            "§3§lSuper Nintendo World §fTrash Can", //91
            "§a§lYoshi §fMerchandise §aKart", // 92
            "§3§lWater§9§lWorld §f§lSpinner", // 93
            "TBD Model", // 94
            "§a§lNEVER SET TO BE A MODEL <PLACEHOLDER NAME>" // Line Above + 1
    };


    @SuppressWarnings("deprecation")
    public static ItemStack createmodel(short id) {
        ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(getLore(id));
        meta.setDisplayName(names[id]);
        meta.setUnbreakable(true);
        Integer intid = new Integer(id);
        meta.setCustomModelData(intid);
        item.setItemMeta(meta);
        return item;
    }

    public static short getId(String str) {

        for(short pos = 0; pos < names.length; pos++) {

            if (str.equalsIgnoreCase(ChatColor.stripColor(names[pos])))
                return pos;

        }

        return -1;

    }

    private static List<String> getLore(short id) {
        List<String> lore = new ArrayList<>();

        switch(id) {
            case 55: case 56: // Can add multiple cases for single lore
                lore.add("§f--------------------");
                lore.add("§aRight click to eat!");
                lore.add("§6(Effects you with Speed)");
                break;
            case 57: case 58: case 59:
                lore.add("§f--------------------");
                lore.add("§aRight click to Drink!");
                lore.add("§6(Effects you with Speed)");
                break; // stops code

        }
        return lore;
    }


 // end

}