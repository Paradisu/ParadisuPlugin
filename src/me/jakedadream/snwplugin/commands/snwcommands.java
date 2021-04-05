package me.jakedadream.snwplugin.commands;

import me.jakedadream.snwplugin.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class snwcommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        short id;

        switch (cmd.getName().toLowerCase()) {


            case "givecoin":
                player.getInventory().addItem(ItemManager.createCoin());
                return true;

            case "givestarcoin":
                player.getInventory().addItem(ItemManager.createStarCoin());
                return true;

            case "llamazbanwand":
                player.getInventory().addItem(ItemManager.createLLamazBanWand());
                return true;

            case "thruwand":
                player.getInventory().addItem(ItemManager.createThruWand());
                return true;

            case "mgive":
                try {
                    id = Short.parseShort(args[0]);
                    player.getInventory().addItem(ItemManager.createmodel(id));
                } catch (Exception ex) {
                    player.sendMessage("§e§l<!> §cDumbass, this isn't a registered number. Please try again, or don't.");
                }
                return true;

            case "mhat":
                try {
                    id = Short.parseShort(args[0]);

                    ItemStack[] armor = player.getInventory().getArmorContents();
                    armor[3] = ItemManager.createmodel(id);
                    player.getInventory().setArmorContents(armor);

                } catch (Exception ex) {
                    player.sendMessage("§e§l<!> §cDumbass, this isn't a registered number. Please try again, or don't.");

                }
                return true;

            case "idlist":
                player.sendMessage("" +
                        "§7| §c§lMODEL ID LIST\n" +
                        "§7|---------------------\n" +
                        "§7|  §aWarp Pipe, §6§l//1\n" +
                        "§7|  §a1/2 Warp Pipe, §6§l//2\n" +
                        "§7|  §7Spawn Lights, §6§l//3\n" +
                        "§7|  §cRed §fLollipop, §6§l//4\n" +
                        "§7|  §cPirahna §aPlant §fUrinal, §6§l//5\n" +
                        "§7|  §eStar §cPost, §6§l//6\n" +
                        "§7|  §4Ribbon, §6§l//7\n" +
                        "§7|  §dPeach's Castle §bStained Glass Window, §6§l//8\n" +
                        "§7|  §f§lNint Sign, §6§l//9\n" +
                        "§7|  §f§lSuper Sign, §6§l//10\n" +
                        "§7|  §f§lEndo Sign, §6§l//11\n" +
                        "§7|  §f§lWorld Sign, §6§l//12\n" +
                        "§7|  §f§lJake's §7§lLong §8§lSword, §6§l//13\n" +
                        "§7|  §e§lQuestion Block, §6§l//14\n" +
                        "§7|  §bPeach's Castle §eWindow, §6§l//15\n" +
                        "§7|  §6Pirahana Plant Spike, §6§l//16\n" +
                        "§7|  §6(NOT CURRENCY) §eGold Coin Prop, §6§l//17\n" +
                        "§7|  §e§lQUESTION MARK COIN, §6§l//18\n" +
                        "§7|  §3§lSNW Light Post #1 (Bottom Half), §6§l//19\n" +
                        "§7|  §3§lSNW Light Post #1 (Top Half), §6§l//20\n" +
                        "§7|  §4Piranha Plant Main (Aero), §6§l//21\n" +
                        "§7|  §4Piranha Plant Jaw (Aero), §6§l//22\n" +
                        "§7|  §aGreen §2Shell, §6§l//23\n" +
                        "§7|  §716 Bit §c§lMario §9§lCap, §6§l//24\n" +
                        "§7|  §3[§cBuilder§3] §e§lHard Hat, §6§l//25\n" +
                        "§7|  §716 Bit §a§lLuigi §9§lCap, §6§l//26\n" +
                        "§7|  §dPeach's §eCrown, §6§l//27\n" +
                        "§7|  §3Sailor §bMoons §ehair, §6§l//28\n" +
                        "§7|  §7§lShark §f§lCap, §6§l//29\n" +
                        "§7|  §3Jaws Boat §fFront, §6§l//30\n" +
                        "§7|  §3Jaws Boat §fBack, §6§l//31\n" +
                        "§7|  §c§lMushroom §f§lTable §7(Medium), §6§l//32\n" +
                        "§7|  §c§lMushroom §f§lTable §7(Small), §6§l//33\n" +
                        "§7|  §c§lMushroom §f§lTable §7(Large), §6§l//34\n" +
                        "§7|  §f§lClouds §f☻, §6§l//35\n" +
                        "§7|  §7§lBullet §7Bill, §6§l//36\n" +
                        "§7|  §a3 Way Warp Pipe, §6§l//37\n" +
                        "§7|  §cMario §eFlag, §6§l//38\n" +
                        "§7|  §cF§6i§er§ce §6F§el§co§6w§ee§cr, §6§l//39\n" +
                        "§7|  §6§lKey §e§lCoin, §6§l//40\n" +
                        "§7|  §eGoodbye §b20§f20 §eGlasses, §6§l//41\n" +
                        "§7|  §7§lDarksaber, §6§l//42\n" +
                        "§7|  §6§lPOW! §4§lBlock, §6§l//43\n" +
                        "§7|  §b§lPOW! §3§lBlock, §6§l//44\n" +
                        "§7|  §7§lJaws §f§lDisplay Shark, §6§l//45\n" +
                        "§7|  §7§lBROKEN MODEL, §6§l//46\n" +
                        "§7|  §fFull White Fence, §6§l//47\n" +
                        "§7|  §fHalf White Fence, §6§l//48\n" +
                        "§7|  §7§lBullet §7Bill Cannon, §6§l//49\n" +
                        "§7|  §c§lMario §9§lWristband, §6§l//50\n" +
                        "§7|  §a§lLuigi §9§lWristband, §6§l//51\n" +
                        "§7|  §a§lYoshi's §f§lEgg, §6§l//52\n" +
                        "§7|  §a§lYoshi, §6§l//53\n" +
                        "§7|  §7§lThwomp!, §6§l//54\n" +
                        "§7|  §c§lMario's §9§lCap §fStrawberry Shortcake sandwich, §6§l//55\n" +
                        "§7|  §a§lLuigi's §9§lCap §fNo-Bake Cheesecake sandwhich, §6§l//56\n" +
                        "§7|  §c§lMario's §f§lStrawberry Soda, §6§l//57\n" +
                        "§7|  §a§lLuigi's §f§lGreen Apple Soda, §6§l//58\n" +
                        "§7|  §e§lPrincess Peach's §f§lPeach Soda, §6§l//59\n" +
                        "§7|  §fZero §4Twos §cHeadband, §6§l//60\n" +
                        "§7|  §aPalutena's §6Magic §3Staff, §6§l//61\n" +
                        "§7|  §7ODM §2Gear, §6§l//62\n" +
                        "§7|  §7ODM §2Gear §7Blade, §6§l//63\n" +
                        "§7|  §eElizabeth §fthe §5§lGoomba, §6§l//64\n" +
                        "§7|  §eBrad §fthe §c§lGoomba, §6§l//65\n" +
                        "§7|  §eHissy §fthe §2§lGoomba, §6§l//66\n" +
                        "§7|  §cPirahna §aPlant §fJaw, §6§l//67\n" +
                        "§7|  §cPirahna §aPlant §fStem, §6§l//68\n" +
                        "§7|  §cPirahna §aPlant §fMouth, §6§l//69\n" +
                        "§7|  §fTex's §dSimp §6Compass, §6§l//70\n" +
                        "§7|  §fSalty's §dSimp §6Compass, §6§l//71\n" +
                        "§7|  §fUnconfirmable's §dSimp §6Compass, §6§l//72\n" +
                        "§7|  §fEboy's §dSimp §6Compass, §6§l//73\n" +
                        "§7|  §fJake's §dSimp §6Compass, §6§l//74\n" +
                        "§7|  §4§lShy §f§lGuy §6§l//75\n" +
                        "§7|  §4§lWanda's Headband §6§l//76\n" +
                        "§7|  §0§lMais §8§lBunny Ears §6§l//77\n" +
                        "§7|  §6§lLlama Hat §6§l//78\n" +
                        "§7|  §b§lLight Blue §fYoshi Ride Cart §6§l//79\n" +
                        "§7|  §a§lLight Green §fYoshi Ride Cart §6§l//80\n" +
                        "§7|  §5§lPurple §fYoshi Ride Cart §6§l//81\n" +
                        "§7|  §6§lOrange §fYoshi Ride Cart §6§l//82\n" +
                        "§7|  §c§lRed §fYoshi Ride Cart §6§l//83\n" +
                        "§7|  §d§lPink §fYoshi Ride Cart §6§l//84\n" +
                        "§7|  §3§lBlue §fYoshi Ride Cart §6§l//85\n" +
                        "§7|  §e§lYellow §fYoshi Ride Cart §6§l//86\n" +
                        "§7|  §fYoshi Ride Cart §cBars §6§l//87\n" +
                        "§7|  §8§lBowser's Castle §7Recycle Can §6§l//88\n" +
                        "§7|  §8§lBowser's Castle §7Trash Can §6§l//89\n" +
                        "§7|  §3§lSuper Nintendo World §fRecycle Can §6§l//90\n" +
                        "§7|  §3§lSuper Nintendo World §fTrash Can §6§l//91\n" +
                        "§7|  §a§lYoshi §fMerchandise §aKart §6§l//92\n" +
                        "§7|  §3§lWater§9§lWorld §f§lSpinner §6§l//92");
                return true;


            case "sc":
                if (player.hasPermission("snw.sc")) {
                    if (args.length == 0) {
                        // /sc (no args)
                        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fIncorrect usage; Please use '/sc <message>'");
                    } else {
                        String allArgs = "";

                        for(String arg : args) {
                            allArgs += arg + " ";
                        }
                        for (World w : Bukkit.getWorlds()) {
                            for (Player p : w.getPlayers()) {
                                if (p.hasPermission("snw.sc")) {
                                    p.sendMessage("§3[§f§lSC§3] §a" + player.getDisplayName() + ":§f " + allArgs);
                                    // /sc <message>;
                                }
                            }
                        }

                    }
                }
                return true;

            case "gmc":
                player.setGameMode(GameMode.CREATIVE);
                return true;


            case "gms":
                player.setGameMode(GameMode.SURVIVAL);
                return true;

            case "gmsp":
                player.setGameMode(GameMode.SPECTATOR);
                return true;

            case "gma":
                player.setGameMode(GameMode.ADVENTURE);
                return true;

            case "nickname":
                if (player.hasPermission("snw.nickname")) {
                    if (args.length == 0) {
                        // /nick (no args)
                        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fIncorrect usage; Please use '/nick <nickname>'");
                    } else {
                        player.setDisplayName(args[0]);
                        // /nick <nickname>;
                    }
                }
        return true;


            default:
                return false;
            //complain
        }

    }

}