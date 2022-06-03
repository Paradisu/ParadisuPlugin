package net.paradisu.ParadisuPlugin.items.models.modelscroller;

import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import net.paradisu.ParadisuPlugin.ParadisuMain;
//import net.paradisu.ParadisuPlugin.modelmanager.PropModelInv;
import net.paradisu.ParadisuPlugin.items.models.ModelItemManager;

public class ModelScrollerEvents implements Listener {

    // private static ResultSet rs;
    // private static Connection connection;

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();



    public int getAmountRandomModel() {
        // int amountofmodels = 0000;
        return 1;

        // int rows = PropModelInv.getRows(modelitemmanager.getHatData());

        // Random random = new Random();
        // int randommodel = random.nextInt(rows) + 1;

        // return randommodel;
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

                        // ArmorStand armorStand = (ArmorStand) e;
                        ((ArmorStand) e).getEquipment().setHelmet(ModelItemManager.createHatModel(modelid));


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
