package me.jakedadream.ParadisuPlugin.modelmanager.modelscroller;

import me.jakedadream.ParadisuPlugin.modelmanager.PropModelInv;
import me.jakedadream.ParadisuPlugin.modelmanager.modelitemmanager;
import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class modelscroller_events implements Listener {

    private static ResultSet rs;
    private static Connection connection;

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();
    String nopermsmsg = paradisumain.NoPermsMessage();
    String noargsmsg = paradisumain.NoArgsMessage();



    public int getAmountRandomModel() {
        int amountofmodels = 0000;

        int rows = PropModelInv.getRows(modelitemmanager.returnHatData());

        Random random = new Random();
        int randommodel = random.nextInt(rows) + 1;

        return randommodel;
    }

    @EventHandler
    public void PlayerClickController(PlayerInteractAtEntityEvent event) {

        World world = event.getRightClicked().getWorld();
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        String ENTITY_NAME = "ms_controller";

        //player.sendMessage("[EVENT] EVENT REGISTERED");

        if (entity.getType() == EntityType.ARMOR_STAND) {
            //player.sendMessage("[EVENT] ARMOR STAND REGISTERED");
            if (entity.getName().contains(ENTITY_NAME)) {
                //player.sendMessage("[EVENT] NAME REGISTERED");

                for (Entity e : world.getEntities()) {
                    if (e.getName().contains("modelscroller_props")) {
                        //player.sendMessage("[EVENT] FOUND SCROLLER REGISTERED");

                        int modelid = getAmountRandomModel();

                        ArmorStand armorStand = (ArmorStand) e;
                        ((ArmorStand) e).getEquipment().setHelmet(modelitemmanager.createHatModel(modelid));


                        player.sendMessage(cmdprefix + " §fNow showing model " + cmdemph + "#" + modelid + "§f.");

                    }

                }
            }
        }



        /*
        if (entity.getType() == EntityType.ARMOR_STAND && entity.getName() == ENTITY_NAME) { //modelscroller


            for (Entity e : world.getEntities()) {

                if (e.getName() == "modelscroller_props") {

                    int modelid = getAmountRandomModel();
                    ItemStack model = modelitemmanager.createPropModel(modelid);

                    ArmorStand as = (ArmorStand) e;

                    as.getEquipment().getHelmet().setType(model.getType());
                    as.getEquipment().getHelmet().setItemMeta(model.getItemMeta());
                    as.getEquipment().getHelmet().setAmount(model.getAmount());

                    player.sendMessage(cmdprefix + " §fNow showing model " + cmdemph + "#" + modelid + "§f.");
                }
            }

        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[!] No ModelScroller EXISTS");
        }
        */
    }





}
