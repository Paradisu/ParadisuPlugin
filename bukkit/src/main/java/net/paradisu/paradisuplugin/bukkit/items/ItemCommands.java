package net.paradisu.paradisuplugin.bukkit.items;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
import cloud.commandframework.annotations.specifier.Greedy;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;
import net.paradisu.paradisuplugin.bukkit.items.common.ingame.ParadisuCoin;
import net.paradisu.paradisuplugin.bukkit.items.common.ingame.StarCoin;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.ParadisuHead;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCommands {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();

    @CommandPermission("paradiu.givecoin")
    @CommandMethod("givecoin")
    public void giveCoin(CommandSender sender) {
        Player p = (Player) sender;
        p.getInventory().addItem(new ParadisuCoin());
        sender.sendMessage(ChatColor.GREEN + "Gave you a coin!");
    }

    @CommandPermission("paradiu.givecoin")
    @CommandMethod("givecoin <player> <amount>")
    public void giveCoinPlayerAmount(CommandSender sender,
                                     @Argument("player") Player player,
                                     @Argument("amount") int amount) {
        player.getInventory().addItem(new ParadisuCoin(amount));
        player.sendMessage(cmdprefix + "§fYou received " + cmdemph + amount + " §fcoins!");    
    }
    

    @CommandPermission("paradisu.givestarcoin")
    @CommandMethod("givestarcoin")
    public void giveStarCoin(CommandSender sender) {
        Player p = (Player) sender;
        p.getInventory().addItem(new StarCoin());
        p.sendMessage(ChatColor.GREEN + "Gave you a star coin!");
    }

    @CommandPermission("paradisu.givestarcoin")
    @CommandMethod("givestarcoin <player> <amount>")
    public void giveStarCoin(CommandSender sender,
                             @Argument("player") Player player,
                             @Argument("amount") int amount) {
        player.getInventory().addItem(new StarCoin(amount));
        player.sendMessage(cmdprefix + "§fYou received " + cmdemph + amount + " §fcoins!");
    }

    @CommandPermission("paradisu.skull")
    @CommandMethod("skull [player]")
    public void skull(CommandSender sender,
                      @Argument("player") Player player) {
        Player p = (Player) sender;
        if (player == null) {
            p.getInventory().addItem(new ParadisuHead(p));
        } else {
            Player target = player;
            p.getInventory().addItem(new ParadisuHead(target));
            p.sendMessage(cmdprefix + "§fWe successfully gave you the head of §3§n" + player.getName() + "§f!");
        }
    }

    @CommandPermission("paradisu.rename")
    @CommandMethod("rename <name>")
    public void rename(CommandSender sender,
                       @Argument("name") @Greedy String name) {
        Player p = (Player) sender;
        if (p.getInventory().getItemInMainHand() == null
                || p.getInventory().getItemInMainHand().getType() == Material.AIR) {
            p.sendMessage(cmdprefix + "§fYou must be holding an item to rename it.");
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    @CommandPermission("paradisu.rename")
    @ProxiedBy("unname")
    @CommandMethod("rename clear")
    public void unname(CommandSender sender) {
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(null);
        item.setItemMeta(meta);
    }

    @CommandPermission("paradisu.shulkerrename")
    @CommandMethod("srename|shulkerrename <name>")
    public void shulkerRename(CommandSender sender,
                              @Argument("name") @Greedy String name) {
        Player p = (Player) sender;
        Material mat = p.getInventory().getItemInMainHand().getType();
        switch (mat) {
            case SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, 
            MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, 
            LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX, 
            LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, 
            BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX, 
            RED_SHULKER_BOX, BLACK_SHULKER_BOX -> {
                ItemStack item = p.getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                item.setItemMeta(meta);
            }
            default -> {
                p.sendMessage(cmdprefix + "§fYou must be holding a shulker box to rename it.");
            }
        }
    }

    @CommandPermission("paradisu.glow")
    @CommandMethod("glow")
    public void glow(CommandSender sender) {
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            p.sendMessage(cmdprefix + "§fYou must be holding an item to glow it.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.ARROW_INFINITE, 4341, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        p.sendMessage(cmdprefix + "§fYou have added glow to your item.");
    }

    @CommandPermission("paradisu.glow")
    @CommandMethod("unglow")
    public void unglow(CommandSender sender) {
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            p.sendMessage(cmdprefix + "§fYou must be holding an item.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.removeEnchant(Enchantment.ARROW_INFINITE);
        meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        p.sendMessage(cmdprefix + "§fYou have removed glow from your item.");
    }

    @CommandPermission("paradisu.speed")
    @CommandMethod("speed <speed>")
    public void speedN(CommandSender sender,
                       @Argument("speed") int speed) {
        Player p = (Player) sender;
        if (speed <= 1) {
            p.setWalkSpeed(0.2f);
            p.setFlySpeed(0.2f);
            p.sendMessage(cmdprefix + "§fReset player speed");
            return;
        } else if (speed <= 10) {
            p.setWalkSpeed(0.1f * speed);
            p.setFlySpeed(0.1f * speed);
            p.sendMessage(cmdprefix + "§fSet player speed to " + speed + "x");
        } else {
            p.sendMessage(cmdprefix + "§fSpeed must be between 1 and 10.");
        }
    }

    @CommandPermission("paradisu.speed")
    @CommandMethod("speed")
    public void speed(CommandSender sender) {
        Player p = (Player) sender;
        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.2f);
        p.sendMessage(cmdprefix + "§fReset player speed");
    }

}
