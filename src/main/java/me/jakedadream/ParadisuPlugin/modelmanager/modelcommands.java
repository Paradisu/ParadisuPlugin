package me.jakedadream.ParadisuPlugin.modelmanager;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Random;

public class modelcommands implements CommandExecutor {

    private String getParsedName(String[] args) {
        String name = "";
        for (String arg : args) {
            name = name.concat(arg);
            name = name.concat(" ");
        }
        return ChatColor.translateAlternateColorCodes('&', name);
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {

            case "mgive":
                if (player.hasPermission("snw.model")) {
                    if(args.length == 0){
                        player.openInventory(PropModelInv.getInvs().get(0));
                    } else if (args.length == 1){
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1){
                            player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have space in your inventory.");
                            return false;
                        }
                        player.getInventory().addItem(modelitemmanager.createPropModel(Integer.parseInt(args[0])));
                    }
                    player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fOpening the Catalog of Default Models!");
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;


            case "hgive":
                if (player.hasPermission("snw.model")) {

                    // hat menu
                    player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fOpening the Catalog of Hat Models!");
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;


            case "mhat":
                if (player.hasPermission("snw.model")) {

                    // model on head

                    player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fPlaced the model <display name> on your head!");
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;

            case "reloadprops":
                paradisumain.reloadPropModelsConfig();
                player.sendMessage("ok");
                break;

            case "createmodelcfsection":
                if (player.hasPermission("snw.model")) {
                    //
                    Random rand = new Random(); Integer upperbound = 1000; Integer int_random = rand.nextInt(upperbound);
                    String randstring = int_random.toString();
                    //
                    if (args.length < 1) {
                        player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §7Please add an argument & specify between Props & Hats.");
                    } else if (args.length == 1) {
                        // ------------------------------------------------------------------------------------------------------------------------------------------------
                        if (args[0].equals("prop")) {
                            //
                            String sectionname = randstring;
                            paradisumain.getPropModelsConfig().createSection(sectionname);
                            ConfigurationSection cs = paradisumain.filePropModelsConfig.getConfigurationSection(sectionname);

//                            cs.set("custommodeldata", int_random);

                            cs.set("displayname", "Unset Model Name");
                            cs.set("enchantslot1", "Enchantment.LUCK");
                            cs.set("enchantslot2", "Enchantment.LUCK");
                            cs.set("enchantslot3", "Enchantment.LUCK");

                            cs.set("enchantslot1level", 0);
                            cs.set("enchantslot2level", 0);
                            cs.set("enchantslot3level", 0);

                            cs.set("lore1", "DEFAULT LORE 1");
                            cs.set("lore2", "DEFAULT LORE 2");
                            cs.set("lore3", "DEFAULT LORE 3");
                            cs.set("lore4", "DEFAULT LORE 4");
                            cs.set("lore5", "DEFAULT LORE 5");
                            cs.set("lore6", "DEFAULT LORE 6");
                            cs.set("lore7", "DEFAULT LORE 7");
                            cs.set("lore8", "DEFAULT LORE 8");
                            cs.set("lore9", "DEFAULT LORE 9");
                            cs.set("lore10", "DEFAULT LORE 10");

                            cs.set("unbreakable", false);
                            cs.set("hideunbreakable", false);
                            cs.set("hideenchants", false);
                            //
                            paradisumain.savePropModelsConfig();
                            //
                            player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fNew Model Section created!");
                            //
                        } else if (args[0].equals("hat")) {
                            //




                            player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fNew Model Section created!");
                            //
                        } else {player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §7Please specify between Props & Hats.");}
                    } else {player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §7Too many args.");}
                    // ------------------------------------------------------------------------------------------------------------------------------------------------
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;




            default:
                return false;
        }
        return false;
    }
}


