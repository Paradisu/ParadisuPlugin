package me.jakedadream.ParadisuPlugin.modelmanager.modelscroller;

import me.jakedadream.ParadisuPlugin.modelmanager.modelitemmanager;
import me.jakedadream.ParadisuPlugin.paradisumain;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class modelscroller_commands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String cmdprefix = paradisumain.CommandPrefix();
        String cmdemph = paradisumain.CommandEmph();
        String nopermsmsg = paradisumain.NoPermsMessage();
        String noargsmsg = paradisumain.NoArgsMessage();

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {

            case "modelscrollerswitch":
                if (player.hasPermission("snw.modelscroller")) {

                    World world = player.getWorld();


                    for (Entity e : world.getEntities()) {
                        if (e.getName() == "modelscroller_props") {

                            modelscroller_events mse = new modelscroller_events();
                            int modelid = mse.getAmountRandomModel();
                            ItemStack model = modelitemmanager.createPropModel(modelid);

                            ArmorStand as = (ArmorStand) e;

                            as.getEquipment().getHelmet().setType(model.getType());
                            as.getEquipment().getHelmet().setItemMeta(model.getItemMeta());
                            as.getEquipment().getHelmet().setAmount(model.getAmount());

                            player.sendMessage(cmdprefix + " §fNow showing model " + cmdemph + "#" + modelid + "§f.");
                        }
                    }
                } else { player.sendMessage(nopermsmsg); }




            default:
                return false;
            //complain
        }
    }
}
