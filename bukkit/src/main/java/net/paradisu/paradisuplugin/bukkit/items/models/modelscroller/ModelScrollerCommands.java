package net.paradisu.paradisuplugin.bukkit.items.models.modelscroller;

import net.paradisu.paradisuplugin.bukkit.items.models.ModelItemManager;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModelScrollerCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String cmdprefix = ParadisuMain.CommandPrefix();
        String cmdemph = ParadisuMain.CommandEmph();
        String nopermsmsg = ParadisuMain.NoPermsMessage();
        // String noargsmsg = ParadisuMain.NoArgsMessage();

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {

            case "modelscrollerswitch":
                if (player.hasPermission("paradisu.modelscroller")) {

                    World world = player.getWorld();


                    for (Entity e : world.getEntities()) {
                        if (e.getName() == "modelscroller_props") {

                            ModelScrollerEvents mse = new ModelScrollerEvents();
                            int modelid = mse.getAmountRandomModel();
                            ItemStack model = ModelItemManager.createPropModel(modelid);

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
